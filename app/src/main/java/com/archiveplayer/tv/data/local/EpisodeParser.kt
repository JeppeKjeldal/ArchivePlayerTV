package com.archiveplayer.tv.data.local

object EpisodeParser {
    fun parse(fileName: String?): Pair<Int?, Int?> {
        if (fileName.isNullOrBlank()) return null to null

        val primaryPattern = Regex("(?i).*[Ss](?:eason)?[\\s._-]*(\\d{1,2})[\\s._-]*[Ee](?:pisode)?[\\s._-]*(\\d{1,2}).*")
        val primaryMatch = primaryPattern.find(fileName)
        if (primaryMatch != null) {
            val season = primaryMatch.groupValues[1].toIntOrNull()
            val episode = primaryMatch.groupValues[2].toIntOrNull()
            return season to episode
        }

        val fallbackPattern = Regex("(?i).*(\\d{1,2})x(\\d{1,2}).*")
        val fallbackMatch = fallbackPattern.find(fileName)
        if (fallbackMatch != null) {
            val season = fallbackMatch.groupValues[1].toIntOrNull()
            val episode = fallbackMatch.groupValues[2].toIntOrNull()
            return season to episode
        }

        return null to null
    }
}