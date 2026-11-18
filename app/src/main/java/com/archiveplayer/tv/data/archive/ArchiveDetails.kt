package com.archiveplayer.tv.data.archive

import android.util.Log

data class ArchiveDetails(
    val identifier: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String,
    val videoFiles: List<String>,
    val allFiles: List<String>
) {
    companion object {
        fun fromMetadata(identifier: String, metadata: Map<String, Any?>): ArchiveDetails {
            val title = (metadata["title"] as? String) ?: identifier
            val description = metadata["description"] as? String

            val files = (metadata["files"] as? List<Map<String, Any>>) ?: emptyList()

            val allFileNames = files.mapNotNull { it["name"] as? String }

            val videoExtensions = listOf("mp4", "mkv", "avi", "webm", "m4v", "ogv", "mov")

            val videoFiles = files
                .filter { file ->
                    val name = file["name"] as? String ?: return@filter false
                    val ext = name.substringAfterLast(".", "").lowercase()
                    ext in videoExtensions
                }
                .mapNotNull { it["name"] as? String }
                .sorted()

            Log.d("ArchiveDetails", "Item: $identifier")
            Log.d("ArchiveDetails", "Total files found: ${allFileNames.size}")
            Log.d("ArchiveDetails", "Video files found: $videoFiles")

            return ArchiveDetails(
                identifier = identifier,
                title = title,
                description = description,
                thumbnailUrl = "https://archive.org/services/img/$identifier",
                videoFiles = videoFiles,
                allFiles = allFileNames.take(10) // Vis max 10 til debugging
            )
        }
    }
}