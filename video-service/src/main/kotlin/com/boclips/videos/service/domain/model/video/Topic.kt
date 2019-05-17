package com.boclips.videos.service.domain.model.video

import com.boclips.events.types.video.VideoAnalysedTopic
import java.util.Locale

data class Topic(
    val name: String,
    val language: Locale,
    val confidence: Double,
    val parent: Topic?
) {
    companion object {

        fun fromAnalysedVideoTopic(assignedTopic: VideoAnalysedTopic): Topic {
            return Topic(
                name = assignedTopic.name,
                language = assignedTopic.language,
                confidence = assignedTopic.confidence,
                parent = assignedTopic.parent?.let(this::fromAnalysedVideoTopic)
            )
        }
    }
}
