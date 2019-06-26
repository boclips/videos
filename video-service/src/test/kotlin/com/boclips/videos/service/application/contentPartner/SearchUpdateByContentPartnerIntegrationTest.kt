package com.boclips.videos.service.application.contentPartner

import com.boclips.events.types.ContentPartnerExclusionFromSearchRequested
import com.boclips.events.types.ContentPartnerInclusionInSearchRequested
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
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

class SearchUpdateByContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videoSearchService: VideoSearchService

    @Test
    fun `disables content partner from search`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        subscriptions.contentPartnerExclusionFromSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    ContentPartnerExclusionFromSearchRequested.builder()
                        .contentPartnerId("deadb33f1225df4825e8b8f6")
                        .build()
                ).build()
            )

        assertThat(
            contentPartnerRepository.findById(ContentPartnerId(value = "deadb33f1225df4825e8b8f6"))?.searchable
        ).isFalse()
    }

    @Test
    fun `enables content partner in search`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        subscriptions.contentPartnerInclusionInSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    ContentPartnerInclusionInSearchRequested.builder()
                        .contentPartnerId("deadb33f1225df4825e8b8f6")
                        .build()
                ).build()
            )

        assertThat(
            contentPartnerRepository.findById(ContentPartnerId(value = "deadb33f1225df4825e8b8f6"))?.searchable
        ).isTrue()
    }

    @Test
    fun `disables videos for that content partner from search`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6", searchable = true)

        subscriptions.contentPartnerExclusionFromSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    ContentPartnerExclusionFromSearchRequested.builder()
                        .contentPartnerId("deadb33f1225df4825e8b8f6")
                        .build()
                ).build()
            )

        val video = videoRepository.find(id)!!
        assertThat(video.contentPartner.searchable).isFalse()
        assertThat(video.searchable).isFalse()
    }

    @Test
    fun `enables videos for that content partner in search`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6", searchable = false)

        subscriptions.contentPartnerInclusionInSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    ContentPartnerInclusionInSearchRequested.builder()
                        .contentPartnerId("deadb33f1225df4825e8b8f6")
                        .build()
                ).build()
            )

        val video = videoRepository.find(id)!!
        assertThat(video.contentPartner.searchable).isTrue()
        assertThat(video.searchable).isTrue()
    }

    @Test
    fun `removes videos from search indices`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6", searchable = true)

        subscriptions.contentPartnerExclusionFromSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    ContentPartnerExclusionFromSearchRequested.builder()
                        .contentPartnerId("deadb33f1225df4825e8b8f6")
                        .build()
                ).build()
            )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(0)
        verify(legacySearchService).bulkRemoveFromSearch(listOf(id.value))
    }

    @Test
    fun `adds videos to search indices`() {
        val id = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6", searchable = false)

        subscriptions.contentPartnerInclusionInSearchRequested()
            .send(
                MessageBuilder.withPayload(
                    ContentPartnerInclusionInSearchRequested.builder()
                        .contentPartnerId("deadb33f1225df4825e8b8f6")
                        .build()
                ).build()
            )

        assertThat(videoSearchService.count(VideoQuery(ids = listOf(id.value)))).isEqualTo(1)
        verify(legacySearchService, times(1)).upsert(any(), anyOrNull())
    }
}