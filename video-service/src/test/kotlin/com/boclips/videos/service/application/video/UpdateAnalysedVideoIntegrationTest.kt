package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysed
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysedKeyword
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysedTopic
import com.boclips.videos.service.testsupport.TestFactories.createKalturaCaptionAsset
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
        val videoAnalysed = createVideoAnalysed(videoId = assetId.value)

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isNotEmpty
    }

    @Test
    fun `does NOT upload captions to Kaltura when transcript has no words`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(videoId = assetId.value, transcript = "\n")

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isEmpty()
    }

    @Test
    fun `deletes existing auto-generated captions when transcript has no words`() {
        val existingCaptions = createKalturaCaptionAsset(
                language = KalturaLanguage.ENGLISH,
                label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionsFile("reference-id", existingCaptions, "bla bla bla")

        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(videoId = assetId.value, transcript = "\n")

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isEmpty()
    }

    @Test
    fun `stores language`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(
                videoId = assetId.value,
                language = Locale.ITALY
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.language).isEqualTo(Locale.ITALY)
    }

    @Test
    fun `stores transcript`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(
                videoId = assetId.value,
                transcript = "bla bla bla"
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `stores topics`() {
        val assetId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(
                videoId = assetId.value,
                topics = listOf(createVideoAnalysedTopic(name = "topic name"))
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

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
        val videoAnalysed = createVideoAnalysed(
                videoId = assetId.value,
                keywords = listOf(
                        createVideoAnalysedKeyword(name = "old keyword 2"),
                        createVideoAnalysedKeyword(name = "new keyword")
                )
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoAssetRepository.find(assetId)!!

        assertThat(video.keywords).containsExactlyInAnyOrder("old keyword 1", "old keyword 2", "new keyword")
    }
}
