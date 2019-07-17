package com.boclips.videos.service.application.video

import com.boclips.events.config.subscriptions.VideosExclusionFromDownloadRequestedSubscription
import com.boclips.events.config.subscriptions.VideosExclusionFromStreamRequestedSubscription
import com.boclips.events.config.subscriptions.VideosInclusionInDownloadRequestedSubscription
import com.boclips.events.config.subscriptions.VideosInclusionInStreamRequestedSubscription
import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import com.boclips.events.types.video.VideosInclusionInDownloadRequested
import com.boclips.events.types.video.VideosInclusionInStreamRequested
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder

class BulkVideoSearchUpdateIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videosExclusionFromStreamRequestedSubscription: VideosExclusionFromStreamRequestedSubscription

    @Autowired
    lateinit var videosExclusionFromDownloadRequestedSubscription: VideosExclusionFromDownloadRequestedSubscription

    @Autowired
    lateinit var videosInclusionInStreamRequestedSubscription: VideosInclusionInStreamRequestedSubscription

    @Autowired
    lateinit var videosInclusionInDownloadRequestedSubscription: VideosInclusionInDownloadRequestedSubscription

    @Test
    fun `removes videos from stream search index`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        videosExclusionFromStreamRequestedSubscription.channel().send(
            MessageBuilder.withPayload(
                VideosExclusionFromStreamRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(0)
    }

    @Test
    fun `removes videos from download search index`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        videosExclusionFromDownloadRequestedSubscription.channel().send(
            MessageBuilder.withPayload(
                VideosExclusionFromDownloadRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        verify(legacyVideoSearchService).bulkRemoveFromSearch(listOf(id.value))
    }

    @Test
    fun `adds videos to stream search index`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        videosInclusionInStreamRequestedSubscription.channel().send(
            MessageBuilder.withPayload(
                VideosInclusionInStreamRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(1)
    }

    @Test
    fun `adds videos to download search index`() {
        val cp = saveContentPartner(distributionMethods = setOf(DistributionMethodResource.STREAM))
        val id = saveVideo(contentProviderId = cp.contentPartnerId.value)

        videosInclusionInDownloadRequestedSubscription.channel().send(
            MessageBuilder.withPayload(
                VideosInclusionInDownloadRequested.builder().videoIds(listOf(id.value)).build()
            ).build()
        )

        verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
    }
}
