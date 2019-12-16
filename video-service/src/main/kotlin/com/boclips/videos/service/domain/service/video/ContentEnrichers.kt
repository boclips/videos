package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video

class ContentEnrichers {
    companion object {
        fun isClassroom(video: Video): Boolean {
            val lowercaseTextFragments = listOf(video.title, video.description).map(String::toLowerCase)
            val lowercaseText = lowercaseTextFragments.joinToString("\n")
            val bagOfWords = wordChars.findAll(lowercaseText).mapTo(mutableSetOf(), MatchResult::value).toSet()

            return when {
                inappropriateVideoIds.contains(video.videoId.value) -> false
                video.type != ContentType.STOCK -> true
                classroomExcludedWords.any { bagOfWords.contains(it) } -> false
                classroomExcludedPhrases.any { lowercaseText.contains(it) } -> false
                classroomPermittedPhrases.any { lowercaseText.contains(it) } -> true
                classroomPermittedNonAdjacentWords.any { bagOfWords.containsAll(it) } -> true
                else -> false
            }
        }

        fun isNews(video: Video): Boolean {
            return video.type == ContentType.NEWS
        }

        private val classroomExcludedWords = listOf(
            "award",
            "awards",
            "cannes",
            "celebrities",
            "celebrity",
            "exchange",
            "hollywood",
            "naked",
            "nude",
            "party",
            "premier",
            "premiere",
            "sexy",
            "topless"
        )

        private val classroomExcludedPhrases = listOf(
            "red carpet",
            "los angeles"
        )

        private val classroomPermittedPhrases = listOf(
            "archive public information film"
        )

        private val classroomPermittedNonAdjacentWords = listOf(
            listOf("speech"),
            listOf("biology", "animation"),
            listOf("space", "animation")
        )

        private val wordChars = Regex("\\w+")

        private val inappropriateVideoIds = listOf(
            "5c54d6a2d8eafeecae205289",
            "5c54da08d8eafeecae222578",
            "5ca450a3a68a191e9b02bd44",
            "5cb8a8aae48a546d9248c31c",
            "5cc870db7c7b937e43460e2b",
            "5cb6002e184a944e461ce8b6",
            "5cb6015d1929942a8465348a",
            "5cb6002d1929942a84653488"
        )
    }
}
