package takagi.ru.monica.utils

import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import okhttp3.Interceptor
import okhttp3.Response
import takagi.ru.monica.mdbx.MdbxDiagLogger

/** No URLs, account IDs, tokens or response bodies are logged. Never retries a write. */
internal class OneDriveRequestMonitor(
    private val clock: () -> Long = System::currentTimeMillis,
    private val cooldowns: ConcurrentHashMap<String, Long> = sharedCooldowns,
    private val record: (String) -> Unit = { MdbxDiagLogger.append(it) }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host
        val start = clock()
        if ((cooldowns[host] ?: 0) > start) throw IOException("OneDrive is cooling down; retry later")
        var status = 0
        try {
            val response = chain.proceed(request)
            status = response.code
            if (status == 429 || status == 503) {
                val header = response.header("Retry-After")
                val delay = header?.toLongOrNull()?.coerceIn(0, 86_400)?.times(1_000)
                    ?: runCatching { ZonedDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant().toEpochMilli() - clock() }.getOrNull() ?: 30_000L
                cooldowns.merge(host, clock() + delay.coerceAtLeast(1_000L), ::maxOf)
            }
            return response
        } finally {
            val kind = when {
                request.url.encodedPath.endsWith("/children") -> "list"
                request.url.encodedPath.endsWith("/content") -> "content"
                else -> "metadata"
            }
            runCatching { record("[OneDrive][request] method=${request.method} kind=$kind status=$status headersMs=${clock() - start}") }
        }
    }
    companion object { private val sharedCooldowns = ConcurrentHashMap<String, Long>() }
}
