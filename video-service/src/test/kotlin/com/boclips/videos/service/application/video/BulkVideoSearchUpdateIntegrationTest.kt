package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import com.boclips.events.types.video.VideosInclusionInDownloadRequested
import com.boclips.events.types.video.VideosInclusionInStreamRequested
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.isNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.util.UUID

class BulkVideoSearchUpdateIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `removes videos from stream search index`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        subscriptions.videosExclusionFromStreamRequested().send(
            MessageBuilder.withPayload(
                VideosExclusionFromStreamRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(0)
    }

    @Test
    fun `removes videos from download search index`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        subscriptions.videosExclusionFromDownloadRequested().send(
            MessageBuilder.withPayload(
                VideosExclusionFromDownloadRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        verify(legacyVideoSearchService).bulkRemoveFromSearch(listOf(id.value))
    }

    @Test
    fun `adds videos to stream search index`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        subscriptions.videosInclusionInStreamRequested().send(
            MessageBuilder.withPayload(
                VideosInclusionInStreamRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(1)
    }

    @Test
    fun `adds videos to download search index`() {
        val cp = saveContentPartner(hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD))
        val id = saveVideo(contentProviderId = cp.contentPartnerId.value)

        subscriptions.videosInclusionInDownloadRequested().send(
            MessageBuilder.withPayload(
                VideosInclusionInDownloadRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
    }

    @Test
    fun `does not add Youtube videos into download search index`() {
        val contentPartner = saveContentPartner(hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD))

        val videoId = saveVideo(
            playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, value = "ref-id-${UUID.randomUUID()}"),
            contentProviderId = contentPartner.contentPartnerId.value

        )

        subscriptions.videosInclusionInDownloadRequested().send(
            MessageBuilder.withPayload(
                VideosInclusionInDownloadRequested.builder().videoIds(listOf(videoId.value)).build()
            ).build()
        )

        verify(legacyVideoSearchService).upsert(argThat { toList().isEmpty() }, isNull())
    }
}
