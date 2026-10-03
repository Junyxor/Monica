package takagi.ru.monica.credentialexchange

import android.app.UiAutomation
import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.viewModelScope
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import takagi.ru.monica.R
import takagi.ru.monica.data.*
import takagi.ru.monica.data.model.ProjectCredentialGroup
import takagi.ru.monica.repository.CustomFieldRepository
import takagi.ru.monica.utils.AppLocaleStringResolver
import takagi.ru.monica.viewmodel.PasswordViewModel
import takagi.ru.monica.data.model.StorageTarget

class ProjectCredentialLiveExportTest {
    @Test fun otherAppReceivesPairedAccountsThroughGooglePickerAfterAuthentication() = runBlocking {
        assumeTrue("Opt in only in existing synthetic test user", android.os.Process.myUid() / 100000 > 0 &&
            InstrumentationRegistry.getArguments().getString("autofillIsolatedUser") == "true")
        val f = TransferFixture()
        val ui = InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        ui.serviceInfo = ui.serviceInfo.apply { flags = flags or 0x40 or 0x2 }
        val vm = PasswordViewModel(f.passwords, f.security, customFieldRepository = CustomFieldRepository(f.db.customFieldDao()),
            context = f.context, localKeePassDatabaseDao = f.db.localKeePassDatabaseDao(), strings = AppLocaleStringResolver(f.context))
        try {
            check(f.security.isMasterPasswordSet() && f.security.unlockVaultWithPassword("Monica-Autofill-Test-2026!")) {
                "Use the existing synthetic autofill user; never reset another vault"
            }
            val destination = f.keepass()
            val title = "${f.prefix}-live"
            val groups = listOf(
                ProjectCredentialGroup.Group(username = "  personal+测试@example.invalid  ", otp = "JBSWY3DPEHPK3PXP",
                    passwords = listOf(ProjectCredentialGroup.Password(value = "  personal-1 🔑  "), ProjectCredentialGroup.Password(value = "personal-2"))),
                ProjectCredentialGroup.Group(username = "work@example.invalid",
                    otp = "otpauth://totp/Work:work%40example.invalid?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ&issuer=Work&algorithm=SHA256&digits=8&period=60",
                    passwords = listOf(ProjectCredentialGroup.Password(value = "work-1"), ProjectCredentialGroup.Password(value = "work-2"))))
            val done = CompletableDeferred<Long?>()
            vm.savePasswordsAcrossTargets(emptyList(), PasswordEntry(title = title, username = "", password = "", website = f.website),
                emptyList(), listOf(StorageTarget.KeePass(destination.databaseId!!, null)), projectCredentials = groups,
                onComplete = { done.complete(it) })
            requireNotNull(withTimeout(60000) { done.await() })
            val before = f.keepassFile(destination.databaseId).readBytes()
            withTimeout(25000) { CredentialExchangeRegistrar.register(f.context) }
            f.context.startActivity(Intent().apply {
                component = ComponentName("takagi.ru.monica.cxptestpeer", "takagi.ru.monica.cxptestpeer.PeerActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("source", f.context.packageName); putExtra("title", title)
            })
            tap(ui, waitNode(ui) { it.packageName?.toString() == "com.google.android.gms" &&
                (it.text?.toString() == "Monica" || it.text?.toString() == "Monica F-Droid") })
            // Verify that the real, caller-validated exporter was opened before interacting.
            waitNode(ui) { it.packageName?.toString() == f.context.packageName && it.text?.contains("CXF Test Peer") == true }
            tap(ui, waitNode(ui) { it.packageName?.toString() == f.context.packageName && it.text?.toString() == "Monica" })
            tap(ui, waitNode(ui) { it.text?.toString() == "${f.prefix}-KDBX" })
            tap(ui, waitNode(ui) { it.text?.toString() == f.context.getString(R.string.exchange_export_verify) })
            val master = waitNode(ui) { it.isEditable && it.packageName?.toString() == f.context.packageName }
            assertTrue(master.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "Monica-Autofill-Test-2026!")
            }))
            tap(ui, waitNode(ui) { it.text?.toString() == f.context.getString(R.string.confirm) })
            tap(ui, waitNode(ui) { it.text?.toString() == f.context.getString(R.string.start_export) })
            waitNode(ui) { it.text?.toString() == "RECEIVER_OK 4 passwords 4 paired OTPs" }
            assertArrayEquals("Export must not rewrite the source vault", before, f.keepassFile(destination.databaseId).readBytes())
        } finally {
            ui.executeShellCommand("am force-stop --user ${android.os.Process.myUid() / 100000} takagi.ru.monica.cxptestpeer").close()
            vm.viewModelScope.cancel(); f.close()
        }
    }

    private fun waitNode(ui: UiAutomation, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val end = SystemClock.elapsedRealtime() + 20000
        do {
            val nodes = ArrayDeque<AccessibilityNodeInfo>()
            ui.rootInActiveWindow?.let(nodes::add); ui.windows.mapNotNull { it.root }.forEach(nodes::add)
            var visited = 0
            while (nodes.isNotEmpty() && visited++ < 1000) {
                val n = nodes.removeFirst()
                if (predicate(n)) return n
                if (n.text?.startsWith("RECEIVER_ERROR") == true || n.text?.startsWith("RECEIVER_INVALID") == true) {
                    throw AssertionError(n.text.toString())
                }
                repeat(n.childCount) { n.getChild(it)?.let(nodes::add) }
            }
            Thread.sleep(75)
        } while (SystemClock.elapsedRealtime() < end)
        throw AssertionError("Expected transfer UI not found; root=${ui.rootInActiveWindow?.packageName}")
    }

    private fun tap(ui: UiAutomation, node: AccessibilityNodeInfo) {
        val r = Rect().also(node::getBoundsInScreen)
        val start = SystemClock.uptimeMillis()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val e = MotionEvent.obtain(start, SystemClock.uptimeMillis(), action, r.exactCenterX(), r.exactCenterY(), 0)
            e.source = InputDevice.SOURCE_TOUCHSCREEN
            try { check(ui.injectInputEvent(e, true)) } finally { e.recycle() }
        }
    }
}
