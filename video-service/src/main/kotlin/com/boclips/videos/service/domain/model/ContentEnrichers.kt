package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.video.LegacyVideoType

class ContentEnrichers {
    companion object {
        fun isClassroom(video: Video): Boolean {
            val lowercaseTextFragments = listOf(video.title, video.description).map(String::toLowerCase)
            val lowercaseText = lowercaseTextFragments.joinToString("\n")
            val bagOfWords = wordChars.findAll(lowercaseText).mapTo(mutableSetOf(), MatchResult::value).toSet()

            return when {
                contentPartnersExcluded.any {
                    video.contentPartnerName.equals(
                        other = it,
                        ignoreCase = true
                    )
                } -> false
                video.type != LegacyVideoType.STOCK -> true
                classroomExcludedWords.any { bagOfWords.contains(it) } -> false
                classroomExcludedPhrases.any { lowercaseText.contains(it) } -> false
                classroomPermittedPhrases.any { lowercaseText.contains(it) } -> true
                classroomPermittedNonAdjacentWords.any { bagOfWords.containsAll(it) } -> true
                else -> false
            }
        }

        fun isNews(video: Video): Boolean {
            return video.type == LegacyVideoType.NEWS
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

        private val contentPartnersExcluded = listOf(
            "AP",
            "NUMBEROCK",
            "Siren Films",
            "StoryFul",
            "Singapore Press Holdings",
            "Mage Math",
            "engVid",
            "1 Minute in a Museum",
            "British Movietone",
            "TED Talks",
            "TED-Ed"
        )
    }
}

