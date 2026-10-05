package takagi.ru.monica.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
        val authManager = OneDriveAuthManager(context)
        val requestedAccountId = arguments.getString(ARG_ACCOUNT_ID)?.trim().orEmpty()
        val cachedSession = runCatching { authManager.getCachedSession() }.getOrNull()
        val accountId = (requestedAccountId.takeIf(String::isNotBlank) ?: cachedSession?.accountId).orEmpty()
        assumeTrue("A cached OneDrive account was not available", accountId.isNotBlank())

        val accessToken = authManager.acquireAccessToken(accountId).accessToken
            ?: error("OneDrive access token unavailable")
        val remoteRoot = "monica-mdbx2-real-onedrive"
        val runId = UUID.randomUUID().toString()
        try {
            withTimeout(REAL_PROVIDER_TIMEOUT_MS) {
                Mdbx2RealWebDavInstrumentedTest().exerciseRealProvider(
                    context = context,
                    providerName = "OneDrive",
                    remoteRoot = remoteRoot,
                    runId = runId,
                    transport = OneDriveMdbxRemoteTransport(context, accountId),
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
                    }
                )
            }
        } finally {
            runCatching {
                OneDriveKeePassFileSource(context, accountId)
                    .deleteEntry("$remoteRoot/$runId")
            }
        }
    }

    companion object {
        private const val ARG_ACCOUNT_ID = "mdbxOneDriveAccountId"
        private const val REAL_PROVIDER_TIMEOUT_MS = 600_000L
    }
}
