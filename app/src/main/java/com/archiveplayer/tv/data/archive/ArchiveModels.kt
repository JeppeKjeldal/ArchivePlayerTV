package com.archiveplayer.tv.data.archive

// Response fra Archive.org advanced search API
data class ArchiveSearchResponse(
    val response: ArchiveResponse?
)

data class ArchiveResponse(
    val numFound: Int,
    val start: Int,
    val docs: List<ArchiveDoc>?
)

data class ArchiveDoc(
    val identifier: String,
    val title: String?,
    val mediatype: String?,
    val description: String?
)

// Forenklet model til UI-brug
data class ArchiveItem(
    val identifier: String,
    val title: String,
    val mediaType: String,
    val thumbnailUrl: String
) {
    companion object {
        fun fromDoc(doc: ArchiveDoc): ArchiveItem {
            return ArchiveItem(
                identifier = doc.identifier,
                title = doc.title ?: doc.identifier,
                mediaType = doc.mediatype ?: "unknown",
                thumbnailUrl = "https://archive.org/services/img/${doc.identifier}"
            )
        }
    }
}