package takagi.ru.monica.utils

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import takagi.ru.monica.localization.xmlTestStrings

class OneDriveSyncEfficiencyTest {
    @Test fun confirmsEachParentOnlyOncePerSession() = runBlocking {
        val server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setBody("""{"id":"folder","name":"folder","folder":{}}""")
        }
        server.start()
        val cache = kotlin.io.path.createTempDirectory().toFile()
        try {
            val source = OneDriveMdbxFileSource(xmlTestStrings("en")) { path ->
                OneDriveKeePassFileSource("fixture", remotePath = path,
                    accessTokenProvider = OneDriveAccessTokenProvider { "fixture" }, httpClient = OkHttpClient(),
                    graphBaseUrl = server.url("/v1.0").toString(), cacheDirectory = cache, strings = xmlTestStrings("en"))
            }
            source.ensureDirectoryPath("sync/device/generation/segments")
            source.ensureDirectoryPath("sync/device/generation/segments")
            source.ensureDirectoryPath("sync/device/generation/blobs")
            assertEquals("Twelve serial metadata checks become five", 5, server.requestCount)
        } finally { server.shutdown(); cache.deleteRecursively() }
    }

    @Test fun throttledRequestsDoNotTouchNetworkUntilRetryAfterExpires() {
        val server = MockWebServer().apply { start() }
        var now = 1_000L
        val client = OkHttpClient.Builder().addInterceptor(OneDriveRequestMonitor({ now }, ConcurrentHashMap(), {})).build()
        val request = Request.Builder().url(server.url("/v1.0/me/drive/root")).build()
        try {
            server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "120"))
            client.newCall(request).execute().close()
            assertTrue(runCatching { client.newCall(request).execute().close() }.exceptionOrNull() is java.io.IOException)
            assertEquals(1, server.requestCount)
            now += 120_000
            server.enqueue(MockResponse().setBody("{}"))
            client.newCall(request).execute().close()
            assertEquals(2, server.requestCount)
        } finally { server.shutdown() }
    }
}
