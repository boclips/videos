package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.Topic
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class TopicDocumentConverterTest {

    @Test
    fun `converts a topic to a document and back`() {
        val originalTopic = Topic(
            name = "the topic",
            language = "pl-PL",
            confidence = 0.3,
            parent = Topic(
                name = "parent topic",
                confidence = 1.0,
                language = "pl-PL",
                parent = Topic(
                    name = "grand parent topic",
                    confidence = 1.0,
                    language = "pl-PL",
                    parent = null
                )
            )
        )

        val document = TopicDocumentConverter.toDocument(originalTopic)
        val restoredTopic = TopicDocumentConverter.toTopic(document)

        Assertions.assertThat(restoredTopic).isEqualTo(originalTopic)
    }
}