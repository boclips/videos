package com.boclips.videos.service.domain.model.asset

import com.boclips.events.types.AnalysedVideoTopic
import java.util.*

data class Topic(
        val name: String,
        val language: Locale,
        val confidence: Double,
        val parent: Topic?
) {
    companion object {

        fun fromAnalysedVideoTopic(assignedTopic: AnalysedVideoTopic): Topic {
            return Topic(
                    name = assignedTopic.name,
                    language = assignedTopic.language,
                    confidence = assignedTopic.confidence,
                    parent = assignedTopic.parent?.let(this::fromAnalysedVideoTopic)
            )
        }

    }
}
