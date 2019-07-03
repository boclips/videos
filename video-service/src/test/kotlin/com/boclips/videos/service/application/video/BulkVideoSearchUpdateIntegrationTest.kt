package com.boclips.videos.service.application.video

import com.boclips.events.types.VideosExclusionFromSearchRequested
import com.boclips.events.types.VideosInclusionInSearchRequested
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
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

    @Test
    fun `removes videos from search indices`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6", searchable = true)

        subscriptions.videosExclusionFromSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    VideosExclusionFromSearchRequested.builder()
                        .videoIds(listOf(id.value))
                        .build()
                ).build()
            )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(0)
        verify(legacyVideoSearchService).bulkRemoveFromSearch(listOf(id.value))
    }

    @Test
    fun `adds videos to search indices`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6", searchable = false)

        subscriptions.videosInclusionInSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    VideosInclusionInSearchRequested.builder()
                        .videoIds(listOf(id.value))
                        .build()
                ).build()
            )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(1)
        verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
    }
}