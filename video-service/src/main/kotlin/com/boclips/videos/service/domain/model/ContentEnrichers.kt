package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoType

class ContentEnrichers {
    companion object {
        fun isNonEducationalStock(videoAsset: VideoAsset): Boolean {
            val lowercaseTextFragments = listOf(videoAsset.title, videoAsset.description).map { it.toLowerCase() }
            val lowercaseText = lowercaseTextFragments.joinToString("\n")
            val bagOfWords = lowercaseTextFragments.flatMap { it.split(" ") }.toSet()

            return when {
                videoAsset.type != VideoType.STOCK -> false
                bagOfWords.contains("awards") -> true
                bagOfWords.contains("premiere") -> true
                lowercaseText.contains("red carpet") -> true
                bagOfWords.contains("speech") -> false
                lowercaseText.contains("archive public information film") -> false
                bagOfWords.containsAll(listOf("biology", "animation")) -> false
                bagOfWords.containsAll(listOf("space", "animation")) -> false
                else -> true
            }
        }
    }
}

