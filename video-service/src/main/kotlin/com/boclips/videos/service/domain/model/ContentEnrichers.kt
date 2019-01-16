package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.LegacyVideoType

class ContentEnrichers {
    companion object {
        fun isClassroom(videoAsset: VideoAsset): Boolean {
            val lowercaseTextFragments = listOf(videoAsset.title, videoAsset.description).map { it.toLowerCase() }
            val lowercaseText = lowercaseTextFragments.joinToString("\n")
            val bagOfWords = lowercaseTextFragments.flatMap { it.split(" ") }.toSet()

            return when {
                videoAsset.type != LegacyVideoType.STOCK -> true
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

        fun isNews(videoAsset: VideoAsset): Boolean {
            return videoAsset.type == LegacyVideoType.NEWS
        }
    }
}

