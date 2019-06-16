package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.infrastructure.contentPartner.MongoContentPartnerRepository
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.time.Duration

class RequestPlaybackUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var requestPlaybackUpdate: RequestPlaybackUpdate

    @BeforeEach
    fun setup() {
        messageCollector.forChannel(topics.videoPlaybackSyncRequested()).clear()
    }

    @Test
    fun `publishes one event per video to be updated`() {
        val video = saveVideo()
        saveVideo()

        requestPlaybackUpdate.invoke()

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)
        Assertions.assertThat(event.videoId).isEqualTo(video.value)
    }
}