package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.time.Duration

class UpdatePlaybackIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var requestPlaybackUpdate: RequestPlaybackUpdate

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
        Assertions.assertThat(updatedAsset.playback).isNotNull
        Assertions.assertThat(updatedAsset.playback.duration).isEqualTo(Duration.ofSeconds(1000))
    }

    @Test
    fun `subscribes to playback sync event and can deal with non-existent youtube videos`() {
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

        requestPlaybackUpdate.invoke(source = "youtube")

        Assertions.assertThat(messageCollector.forChannel(topics.videoPlaybackSyncRequested()).size).isEqualTo(1)

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)

        Assertions.assertThat(event.videoId).isEqualTo(youtube.value)
    }

    @Test
    fun `only events for kaltura videos get published when source is kaltura`() {
        saveVideo(playbackId = PlaybackId(value = "1233", type = PlaybackProviderType.YOUTUBE))
        val kaltura = saveVideo(playbackId = PlaybackId(value = "12331", type = PlaybackProviderType.KALTURA))

        requestPlaybackUpdate.invoke(source = "kaltura")

        Assertions.assertThat(messageCollector.forChannel(topics.videoPlaybackSyncRequested()).size).isEqualTo(1)

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)

        Assertions.assertThat(event.videoId).isEqualTo(kaltura.value)
    }

    @Test
    fun `throws for invalid source`() {
        org.junit.jupiter.api.assertThrows<InvalidSourceException> {
            requestPlaybackUpdate.invoke(source = "blah")
        }
    }
}