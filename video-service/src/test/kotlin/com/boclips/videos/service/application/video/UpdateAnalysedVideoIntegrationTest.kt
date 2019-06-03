package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.InMemoryVideoSearchService
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createKalturaCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysed
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysedKeyword
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysedTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.util.Locale

class UpdateAnalysedVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videoSearchService: InMemoryVideoSearchService

    @Test
    fun `uploads captions to Kaltura`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(videoId = videoId.value)

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isNotEmpty
    }

    @Test
    fun `does NOT upload captions to Kaltura when transcript has no words`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(videoId = videoId.value, transcript = "\n")

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

        val videoId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(videoId = videoId.value, transcript = "\n")

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isEmpty()
    }

    @Test
    fun `stores language`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(
            videoId = videoId.value,
            language = Locale.ITALY
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoRepository.find(videoId)!!

        assertThat(video.language).isEqualTo(Locale.ITALY)
    }

    @Test
    fun `stores transcript`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(
            videoId = videoId.value,
            transcript = "bla bla bla"
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoRepository.find(videoId)!!

        assertThat(video.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `stores topics`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = KALTURA, value = "reference-id"))
        val videoAnalysed = createVideoAnalysed(
            videoId = videoId.value,
            topics = listOf(createVideoAnalysedTopic(name = "topic name"))
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoRepository.find(videoId)!!

        assertThat(video.topics).hasSize(1)
        assertThat(video.topics.first().name).isEqualTo("topic name")
    }

    @Test
    fun `stores merged keywords`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = KALTURA, value = "reference-id"),
            keywords = listOf("old keyword 1", "old keyword 2")
        )
        val videoAnalysed = createVideoAnalysed(
            videoId = videoId.value,
            keywords = listOf(
                createVideoAnalysedKeyword(name = "old keyword 2"),
                createVideoAnalysedKeyword(name = "new keyword")
            )
        )

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        val video = videoRepository.find(videoId)!!

        assertThat(video.keywords).containsExactlyInAnyOrder("old keyword 1", "old keyword 2", "new keyword")
    }

    @Test
    fun `updates the video in the search index`() {
        val videoId = saveVideo()

        val videoAnalysed = createVideoAnalysed(videoId = videoId.value, transcript = "the transcript")

        subscriptions.videoAnalysed().send(MessageBuilder.withPayload(videoAnalysed).build())

        assertThat(
            videoSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "transcript"
                    )
                )
            )
        ).containsExactly(videoId.value)
    }
}
