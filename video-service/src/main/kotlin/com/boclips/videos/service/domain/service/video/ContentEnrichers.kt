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

    }
}
