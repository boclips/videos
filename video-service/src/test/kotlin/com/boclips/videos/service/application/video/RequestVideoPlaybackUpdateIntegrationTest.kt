package com.boclips.videos.service.application.video

import com.boclips.events.types.VideoPlaybackSyncRequested
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.time.Duration

class RequestVideoPlaybackUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var requestVideoPlaybackUpdate: RequestVideoPlaybackUpdate

    @Autowired
    lateinit var videoAssetRepository: MongoVideoAssetRepository

    @Test
    fun `publishes one event per video to be updated`() {
        val asset = saveVideo()
        saveVideo()

        requestVideoPlaybackUpdate.invoke()

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)
        Assertions.assertThat(event.videoId).isEqualTo(asset.value)
    }

    @Test
    fun `subscribes to video playback sync request event and handles it`() {
        val playbackId = TestFactories.createKalturaPlayback().id
        val assetId = saveVideo(
            playbackId = playbackId
        )

        val event = VideoPlaybackSyncRequested.builder().videoId(assetId.value).build()

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

        val updatedAsset = videoAssetRepository.find(assetId)!!
        assertThat(updatedAsset.playback).isNotNull
        assertThat(updatedAsset.playback!!.duration).isEqualTo(Duration.ofSeconds(1000))
    }

    @Test
    fun `subscribes to playback sync event and can deal with inexistent youtube videos`() {
        val playbackId = TestFactories.createYoutubePlayback().id
        val assetId = saveVideo(
            playbackId = playbackId
        )

        fakeYoutubePlaybackProvider.clear()
        val event = VideoPlaybackSyncRequested.builder().videoId(assetId.value).build()

        subscriptions
            .videoPlaybackSyncRequested()
            .send(MessageBuilder.withPayload(event).build())
    }
}