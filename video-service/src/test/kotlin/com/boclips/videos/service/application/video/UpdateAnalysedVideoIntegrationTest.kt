package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createAnalysedVideo
import com.boclips.videos.service.testsupport.TestFactories.createAnalysedVideoKeyword
import com.boclips.videos.service.testsupport.TestFactories.createAnalysedVideoTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.util.*

class UpdateAnalysedVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoAssetRepository: VideoAssetRepository

    @Test
    fun `uploads captions to Kaltura`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val analysedVideo = createAnalysedVideo(videoId = assetId.value)

        subscriptions.analysedVideos().send(MessageBuilder.withPayload(analysedVideo).build())

        assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isNotEmpty
    }

    @Test
    fun `stores language`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val analysedVideo = createAnalysedVideo(
                videoId = assetId.value,
                language = "it-IT"
        )

        subscriptions.analysedVideos().send(MessageBuilder.withPayload(analysedVideo).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.language).isEqualTo(Locale.ITALY)
    }

    @Test
    fun `stores transcript`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val analysedVideo = createAnalysedVideo(
                videoId = assetId.value,
                transcript = "bla bla bla"
        )

        subscriptions.analysedVideos().send(MessageBuilder.withPayload(analysedVideo).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `stores topics`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val analysedVideo = createAnalysedVideo(
                videoId = assetId.value,
                topics = listOf(createAnalysedVideoTopic(name = "topic name"))
        )

        subscriptions.analysedVideos().send(MessageBuilder.withPayload(analysedVideo).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.topics).hasSize(1)
        assertThat(video.topics.first().name).isEqualTo("topic name")
    }

    @Test
    fun `stores merged keywords`() {
        val assetId = saveVideo(
                playbackId = PlaybackId(type = KALTURA, value = "reference-id"),
                keywords = listOf("old keyword 1", "old keyword 2")
        )
        val analysedVideo = createAnalysedVideo(
                videoId = assetId.value,
                keywords = listOf(
                        createAnalysedVideoKeyword(name = "old keyword 2"),
                        createAnalysedVideoKeyword(name = "new keyword")
                )
        )

        subscriptions.analysedVideos().send(MessageBuilder.withPayload(analysedVideo).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.keywords).containsExactlyInAnyOrder("old keyword 1", "old keyword 2", "new keyword")
    }
}
