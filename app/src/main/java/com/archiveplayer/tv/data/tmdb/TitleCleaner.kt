package com.archiveplayer.tv.data.tmdb

object TitleCleaner {
    fun cleanForSearch(title: String): String {
        return title
            .replace(Regex("[-_]?dktale.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[_]\\d+$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\([^)]*\\)"), "")
            .replace(Regex("\\s*-\\s*S\\d+E\\d+.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\d+x\\d+.*", RegexOption.IGNORE_CASE), "")
            .replace("-", " ")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}