package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.testsupport.TestFactories.createAnalysedVideoTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class TopicTest {

    @Test
    fun `copies the name from assigned topic`() {
        val assignedTopic = createAnalysedVideoTopic(name = "the name")

        val topic = Topic.fromAnalysedVideoTopic(assignedTopic)

        assertThat(topic.name).isEqualTo("the name")
    }

    @Test
    fun `copies the language from assigned topic`() {
        val assignedTopic = createAnalysedVideoTopic(language = Locale.CHINA)

        val topic = Topic.fromAnalysedVideoTopic(assignedTopic)

        assertThat(topic.language).isEqualTo(Locale.CHINA)
    }

    @Test
    fun `copies confidence from assigned topic`() {
        val assignedTopic = createAnalysedVideoTopic(confidence = 0.67)

        val topic = Topic.fromAnalysedVideoTopic(assignedTopic)

        assertThat(topic.confidence).isEqualTo(0.67)
    }

    @Test
    fun `parent is null when assigned topic has no parent`() {
        val assignedTopic = createAnalysedVideoTopic(parent = null)

        val topic = Topic.fromAnalysedVideoTopic(assignedTopic)

        assertThat(topic.parent).isNull()
    }

    @Test
    fun `parent is set when assigned topic has a parent`() {
        val assignedTopic = createAnalysedVideoTopic(parent = createAnalysedVideoTopic(name = "parent"))

        val topic = Topic.fromAnalysedVideoTopic(assignedTopic)

        assertThat(topic.parent).isNotNull
        assertThat(topic.parent?.name).isEqualTo("parent")
    }
}
