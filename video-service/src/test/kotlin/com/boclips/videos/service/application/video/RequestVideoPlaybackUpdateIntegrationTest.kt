package com.boclips.videos.service.application.video

import com.boclips.events.types.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
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

class RequestVideoPlaybackUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var requestVideoPlaybackUpdate: RequestVideoPlaybackUpdate

    @Autowired
    lateinit var videoRepository: MongoVideoRepository

    @BeforeEach
    fun setup() {
        messageCollector.forChannel(topics.videoPlaybackSyncRequested()).clear()
    }

    @Test
    fun `publishes one event per video to be updated`() {
        val video = saveVideo()
        saveVideo()

        requestVideoPlaybackUpdate.invoke()

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)
        Assertions.assertThat(event.videoId).isEqualTo(video.value)
    }

    @Test
    fun `subscribes to video playback sync request event and handles it`() {
        val playbackId = TestFactories.createKalturaPlayback().id
        val videoId = saveVideo(
            playbackId = playbackId
        )

        val event = VideoPlaybackSyncRequested.builder().videoId(videoId.value).build()

        fakeKalturaClient.clear()
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                referenceId = playbackId.value,
                duration = Duration.ofSeconds(1000)
            )
        )

        subscriptions
            .videoPlaybackSyncRequested()
            .send(MessageBuilder.withPayload(event).build())

        val updatedAsset = videoRepository.find(videoId)!!
        assertThat(updatedAsset.playback).isNotNull
        assertThat(updatedAsset.playback.duration).isEqualTo(Duration.ofSeconds(1000))
    }

    @Test
    fun `subscribes to playback sync event and can deal with inexistent youtube videos`() {
        val playbackId = TestFactories.createYoutubePlayback().id
        val videoId = saveVideo(
            playbackId = playbackId
        )

        fakeYoutubePlaybackProvider.clear()
        val event = VideoPlaybackSyncRequested.builder().videoId(videoId.value).build()

        subscriptions
            .videoPlaybackSyncRequested()
            .send(MessageBuilder.withPayload(event).build())
    }

    @Test
    fun `only events for youtube videos get published when source is youtube`() {
        val youtube = saveVideo(playbackId = PlaybackId(value = "1233", type = PlaybackProviderType.YOUTUBE))
        saveVideo(playbackId = PlaybackId(value = "12331", type = PlaybackProviderType.KALTURA))

        requestVideoPlaybackUpdate.invoke(source = "youtube")

        assertThat(messageCollector.forChannel(topics.videoPlaybackSyncRequested()).size).isEqualTo(1)

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)

        Assertions.assertThat(event.videoId).isEqualTo(youtube.value)
    }

    @Test
    fun `only events for kaltura videos get published when source is kaltura`() {
        saveVideo(playbackId = PlaybackId(value = "1233", type = PlaybackProviderType.YOUTUBE))
        val kaltura = saveVideo(playbackId = PlaybackId(value = "12331", type = PlaybackProviderType.KALTURA))

        requestVideoPlaybackUpdate.invoke(source = "kaltura")

        assertThat(messageCollector.forChannel(topics.videoPlaybackSyncRequested()).size).isEqualTo(1)

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)

        Assertions.assertThat(event.videoId).isEqualTo(kaltura.value)
    }

    @Test
    fun `throws for invalid source`() {
        assertThrows<InvalidSourceException> {
            requestVideoPlaybackUpdate.invoke(source = "blah")
        }
    }
}