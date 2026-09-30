package takagi.ru.monica.utils

import java.util.Date

/** Both views of one fresh, fully paginated directory response; no persistent cache. */
internal class OneDriveBackupDirectory(entries: List<FileSourceEntry>) {
    val folders: List<FileSourceEntry> = entries.filter { it.isDirectory }
    val backups: List<BackupFile> = entries
        .filter { !it.isDirectory && it.name.endsWith(".zip", ignoreCase = true) }
        .map { entry ->
            BackupFile(
                name = entry.name,
                path = entry.path,
                size = entry.sizeBytes ?: 0L,
                modified = Date(entry.lastModified ?: System.currentTimeMillis())
            )
        }
        .sortedByDescending { it.modified.time }
}
