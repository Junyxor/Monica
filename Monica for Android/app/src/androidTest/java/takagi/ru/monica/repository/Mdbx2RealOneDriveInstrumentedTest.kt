package takagi.ru.monica.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import takagi.ru.monica.data.MdbxRemoteSource
import takagi.ru.monica.data.MdbxSourceType
import takagi.ru.monica.utils.OneDriveAuthManager
import takagi.ru.monica.utils.OneDriveKeePassFileSource
import takagi.ru.monica.utils.OneDriveMdbxRemoteTransport

@RunWith(AndroidJUnit4::class)
class Mdbx2RealOneDriveInstrumentedTest {
    @Test
    fun interactiveSignIn() = runBlocking {
        assumeTrue("Interactive login explicitly enabled", InstrumentationRegistry.getArguments().getString("interactiveOneDriveLogin") == "true")
        val result = kotlinx.coroutines.CompletableDeferred<Unit>()
        val scenario = androidx.test.core.app.ActivityScenario.launch(androidx.activity.ComponentActivity::class.java)
        try {
            scenario.onActivity { activity ->
                activity.setShowWhenLocked(true)
                activity.setTurnScreenOn(true)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try { OneDriveAuthManager(activity).signIn(activity); result.complete(Unit) }
                    catch (e: Exception) { result.completeExceptionally(e) }
                }
            }
            withTimeout(600_000L) { result.await() }
        } finally { scenario.close() }
    }

    @Test
    fun realOneDriveBootstrapSyncAttachmentConflictAndReopen() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val arguments = InstrumentationRegistry.getArguments()
        // A cached personal account must never opt ordinary test runs into cloud writes.
        assumeTrue("Real OneDrive test explicitly enabled", arguments.getString("mdbxRealOneDrive") == "true")
        val authManager = OneDriveAuthManager(context)
        val requestedAccountId = arguments.getString(ARG_ACCOUNT_ID)?.trim().orEmpty()
        val cachedSession = authManager.getCachedSession()
        val accountId = (requestedAccountId.takeIf(String::isNotBlank) ?: cachedSession?.accountId).orEmpty()
        check(accountId.isNotBlank()) { "Explicit OneDrive test requires an authenticated account" }

        val accessToken = authManager.acquireAccessToken(accountId).accessToken
            ?: error("OneDrive access token unavailable")
        val remoteRoot = "monica-mdbx2-real-onedrive"
        val runId = UUID.randomUUID().toString()
        val runPath = "$remoteRoot/$runId"
        val transport = OneDriveMdbxRemoteTransport(context, accountId)
        fun stage(name: String) = instrumentation.sendStatus(2, android.os.Bundle().apply {
            putString("onedriveStage", name)
            // Only the synthetic path, never account identifiers or authentication data.
            putString("onedriveTestDirectory", runPath)
        })
        var primaryFailure: Throwable? = null
        stage("cached_login_ready")
        try {
            withTimeout(REAL_PROVIDER_TIMEOUT_MS) {
                Mdbx2RealWebDavInstrumentedTest().exerciseRealProvider(
                    context = context,
                    providerName = "OneDrive",
                    remoteRoot = remoteRoot,
                    runId = runId,
                    transport = transport,
                    sourceType = MdbxSourceType.REMOTE_ONEDRIVE,
                    sourceFactory = { remoteSourceDao, securityManager, displayName, remotePath ->
                        remoteSourceDao.insertSource(
                            MdbxRemoteSource(
                                displayName = "$displayName ${UUID.randomUUID()}",
                                remotePath = remotePath,
                                remoteParentPath = remotePath.substringBeforeLast('/', "").ifBlank { null },
                                baseUrl = null,
                                usernameEncrypted = securityManager.encryptData(accountId),
                                passwordEncrypted = securityManager.encryptData(accessToken)
                            )
                        )
                    },
                    onSyncTiming = { name, elapsedMillis ->
                        instrumentation.sendStatus(2, android.os.Bundle().apply {
                            putString("onedriveSyncStep", name)
                            putLong("onedriveSyncElapsedMillis", elapsedMillis)
                        })
                    }
                )
                stage("native_roundtrip_attachment_recovery_conflict_reopen_complete")
            }
        } catch (failure: Throwable) {
            primaryFailure = failure
            throw failure
        } finally {
            try {
                withContext(NonCancellable) {
                    withTimeout(90_000L) {
                        if (transport.stat(runPath) != null) {
                            OneDriveKeePassFileSource(context, accountId).deleteEntry(runPath)
                        }
                        assertNull("Synthetic OneDrive directory remains after cleanup", transport.stat(runPath))
                        stage("synthetic_directory_cleanup_verified")
                    }
                }
            } catch (cleanupFailure: Throwable) {
                stage("synthetic_directory_cleanup_failed")
                primaryFailure?.addSuppressed(cleanupFailure) ?: throw cleanupFailure
            }
        }
    }

    companion object {
        private const val ARG_ACCOUNT_ID = "mdbxOneDriveAccountId"
        private const val REAL_PROVIDER_TIMEOUT_MS = 600_000L
    }
}
