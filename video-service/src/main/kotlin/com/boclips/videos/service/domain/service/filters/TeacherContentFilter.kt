package com.boclips.videos.service.domain.service.filters

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoDetails
import com.boclips.videos.service.domain.model.VideoType

class TeacherContentFilter {

    fun showInTeacherProduct(videoDetails: VideoDetails): Boolean {
        if (videoDetails.type != VideoType.STOCK) {
            return true
        }

        val lowercaseTextFragments = listOf(videoDetails.title, videoDetails.description).map { it.toLowerCase() }
        val lowercaseText = lowercaseTextFragments.joinToString("\n")
        val bagOfWords = lowercaseTextFragments.flatMap { it.split(" ") }.toSet()

        return when {
            bagOfWords.contains("awards") -> false
            bagOfWords.contains("premiere") -> false
            lowercaseText.contains("red carpet") -> false
            bagOfWords.contains("speech") -> true
            lowercaseText.contains("archive public information film") -> true
            bagOfWords.containsAll(listOf("biology", "animation")) -> true
            bagOfWords.containsAll(listOf("space", "animation")) -> true
            else -> false
        }
    }

}