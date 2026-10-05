package takagi.ru.monica.utils

import takagi.ru.monica.R

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock

class OneDriveMdbxFileSource internal constructor(
    private val strings: StringResolver,
    private val sourceFactory: (String?) -> OneDriveKeePassFileSource
) : MdbxFileSource {
    constructor(context: Context, accountId: String) : this(AppLocaleStringResolver(context),
        { path -> OneDriveKeePassFileSource(context, accountId, remotePath = path) })

    private val directoryMutex = kotlinx.coroutines.sync.Mutex()
    private val confirmedDirectories = mutableSetOf<String>()
    private fun delegate(remotePath: String? = null) = sourceFactory(remotePath)

    override suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { delegate().testConnection().getOrThrow() }
    }

    override suspend fun listDirectory(path: String?): List<FileSourceEntry> =
        withContext(Dispatchers.IO) {
            val normalizedPath = OneDriveKeePassFileSource.normalizeOptionalRemotePath(path)
            listDirectoryAll(normalizedPath)
                .filter { it.isDirectory || it.name.endsWith(".mdbx", ignoreCase = true) }
                .sortedWith(
                    compareByDescending<FileSourceEntry> { it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )
        }

    suspend fun listDirectoryAll(path: String?): List<FileSourceEntry> =
        withContext(Dispatchers.IO) {
            val normalizedPath = OneDriveKeePassFileSource.normalizeOptionalRemotePath(path)
            delegate().listDirectory(normalizedPath)
        }

    override suspend fun createDirectory(
        parentPath: String?,
        name: String
    ): FileSourceEntry = withContext(Dispatchers.IO) {
        delegate().createDirectory(parentPath, name)
    }

    override suspend fun createPlaceholderFile(
        parentPath: String?,
        name: String
    ): FileSourceEntry = withContext(Dispatchers.IO) {
        delegate().createFileInDirectory(parentPath, name, ByteArray(0))
    }

    override suspend fun writeFile(
        parentPath: String?,
        name: String,
        bytes: ByteArray
    ): FileSourceEntry = withContext(Dispatchers.IO) {
        val normalizedParentPath = OneDriveKeePassFileSource.normalizeOptionalRemotePath(parentPath)
        val targetPath = OneDriveKeePassFileSource.buildChildPath(normalizedParentPath, name, strings = strings)
        val writeResult = delegate(remotePath = targetPath).write(bytes, expectedVersion = null)
        FileSourceEntry(
            name = name,
            path = targetPath,
            isDirectory = false,
            versionToken = writeResult.versionToken,
            lastModified = writeResult.lastModified,
            sizeBytes = bytes.size.toLong()
        )
    }

    override suspend fun readFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        delegate(path).read()
    }

    suspend fun statPath(path: String): FileSourceStat? = withContext(Dispatchers.IO) {
        try {
            delegate(path).stat()
        } catch (error: OneDriveHttpException) {
            if (error.statusCode == 404) null else throw error
        }
    }

    suspend fun readFileTo(path: String, destination: File) = withContext(Dispatchers.IO) {
        delegate(path).readTo(destination)
    }

    suspend fun writeFileFrom(
        path: String,
        source: File,
        mode: MdbxRemoteWriteMode = MdbxRemoteWriteMode.CREATE_ONLY,
        expectedVersion: String? = null
    ): FileSourceWriteResult = withContext(Dispatchers.IO) {
        val parent = OneDriveKeePassFileSource.parentPathOf(path, strings = strings)
        if (parent.isNotBlank()) ensureDirectoryPath(parent)
        try {
            delegate(path).writeFrom(source, mode, expectedVersion)
        } catch (error: OneDriveHttpException) {
            // A folder removed remotely invalidates this session cache. Keep the original
            // conditional write intent and let the durable job retry, never overwrite blindly.
            if (error.statusCode == 404) directoryMutex.withLock { confirmedDirectories.clear() }
            throw error
        }
    }

    suspend fun ensureDirectoryPath(path: String) = withContext(Dispatchers.IO) {
      directoryMutex.withLock {
        val segments = OneDriveKeePassFileSource.normalizeOptionalRemotePath(path)
            .split('/')
            .filter(String::isNotBlank)
        var current = ""
        for (segment in segments) {
            val next = OneDriveKeePassFileSource.buildChildPath(current, segment, strings = strings)
            if (next !in confirmedDirectories) {
                val existing = statPath(next)
                when {
                    existing == null -> createDirectory(current.ifBlank { null }, segment)
                    !existing.isDirectory -> error(strings.get(R.string.cloud_message_not_directory, next))
                }
                confirmedDirectories += next
            }
            current = next
        }
      }
    }
}
