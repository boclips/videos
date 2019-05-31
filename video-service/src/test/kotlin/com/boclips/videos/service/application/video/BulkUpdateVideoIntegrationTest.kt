package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.legacy.SolrDocumentNotFound
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.isNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class BulkUpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videoSearchService: VideoSearchService

    @Test
    fun `disableFromSearch sets searchable field on video video to false and removes from search indices`() {
        val videoIds = listOf(saveVideo(searchable = true), saveVideo(searchable = true))
        bulkUpdateVideo(
            BulkUpdateRequest(
                ids = videoIds.map { it.value },
                status = VideoResourceStatus.SEARCH_DISABLED
            )
        )

        assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable == false }

        assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(0)
        videoIds.forEach { verify(legacySearchService).removeFromSearch(it.value) }
    }

    @Test
    fun `disableFromSearch ignores Solr document not found errors`() {
        Mockito.`when`(legacySearchService.removeFromSearch(any())).doAnswer {
            throw SolrDocumentNotFound(videoId = "")
        }

        val bulkUpdateRequest = BulkUpdateRequest(
            ids = listOf(TestFactories.aValidId()),
            status = VideoResourceStatus.SEARCH_DISABLED
        )

        assertDoesNotThrow { bulkUpdateVideo(bulkUpdateRequest) }
    }

    @Test
    fun `makeSearchable sets searchable field on video video to true and registers in search indices`() {
        val videoIds = listOf(saveVideo(searchable = false), saveVideo(searchable = false))
        bulkUpdateVideo(
            BulkUpdateRequest(
                ids = videoIds.map { it.value },
                status = VideoResourceStatus.SEARCH_DISABLED
            )
        )

        bulkUpdateVideo(BulkUpdateRequest(ids = videoIds.map { it.value }, status = VideoResourceStatus.SEARCHABLE))

        assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable == true }
        assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(2)
        verify(legacySearchService, times(3)).upsert(any(), anyOrNull())
    }

    @Test
    fun `makeSearchable sets searchable field on video video to true and does not register youtube videos in legacy search index`() {
        val videoId = saveVideo(
            searchable = false,
            playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, value = "ref-id-${UUID.randomUUID()}")
        )

        bulkUpdateVideo(BulkUpdateRequest(ids = listOf(videoId.value), status = VideoResourceStatus.SEARCH_DISABLED))

        bulkUpdateVideo(BulkUpdateRequest(ids = listOf(videoId.value), status = VideoResourceStatus.SEARCHABLE))

        verify(legacySearchService).upsert(argThat { toList().isEmpty() }, isNull())
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}