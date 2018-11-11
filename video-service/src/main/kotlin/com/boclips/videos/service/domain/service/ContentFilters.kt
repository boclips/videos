package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoType

class ContentFilters {
    companion object {
        fun isInTeacherProduct(videoAsset: VideoAsset): Boolean {
            if (videoAsset.type != VideoType.STOCK) {
                return true
            }

            val lowercaseTextFragments = listOf(videoAsset.title, videoAsset.description).map { it.toLowerCase() }
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


}

