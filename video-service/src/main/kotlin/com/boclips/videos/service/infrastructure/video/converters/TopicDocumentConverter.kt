package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.infrastructure.video.TopicDocument
import java.util.Locale

object TopicDocumentConverter {

    fun toDocument(topic: Topic): TopicDocument {
        return TopicDocument(
            name = topic.name,
            language = topic.language.toLanguageTag(),
            confidence = topic.confidence,
            parent = topic.parent?.let {
                toDocument(
                    it
                )
            }
        )
    }

    fun toTopic(topicDocument: TopicDocument): Topic {
        return Topic(
            name = topicDocument.name,
            language = Locale.forLanguageTag(topicDocument.language),
            confidence = topicDocument.confidence,
            parent = topicDocument.parent?.let {
                toTopic(
                    it
                )
            }
        )
    }
}
