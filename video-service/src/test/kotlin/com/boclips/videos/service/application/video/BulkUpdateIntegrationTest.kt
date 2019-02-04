package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.Query
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class BulkUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoAssetRepository: VideoAssetRepository

    @Autowired
    lateinit var searchService: SearchService

    @Test
    fun `disableFromSearch sets searchable field on video asset to false and removes from search indices`() {
        val videoIds = listOf(saveVideo(searchable = true), saveVideo(searchable = true))
        bulkUpdate.execute(BulkUpdateRequest(ids = videoIds.map { it.value }, status = VideoResourceStatus.SEARCH_DISABLED))

        assertThat(videoAssetRepository.findAll(videoIds)).allMatch { it.searchable == false }

        assertThat(searchService.count(Query(ids = videoIds.map { it.value }))).isEqualTo(0)
        videoIds.forEach { verify(legacySearchService).removeFromSearch(it.value) }
    }

    @Test
    fun `makeSearchable sets searchable field on video asset to true and registers in search indices`() {
        val videoIds = listOf(saveVideo(searchable = false), saveVideo(searchable = false))
        bulkUpdate.execute(BulkUpdateRequest(ids = videoIds.map { it.value }, status = VideoResourceStatus.SEARCH_DISABLED))

        bulkUpdate.execute(BulkUpdateRequest(ids = videoIds.map { it.value }, status = VideoResourceStatus.SEARCHABLE))

        assertThat(videoAssetRepository.findAll(videoIds)).allMatch { it.searchable == true }
        assertThat(searchService.count(Query(ids = videoIds.map { it.value }))).isEqualTo(2)
        verify(legacySearchService, times(3)).upsert(any(), anyOrNull())
    }

    @Test
    fun `makeSearchable sets searchable field on video asset to true and does not register youtube videos in legacy search index`() {
        val videoId = saveVideo(searchable = false, playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, value = "ref-id-${UUID.randomUUID()}"))

        bulkUpdate.execute(BulkUpdateRequest(ids = listOf(videoId.value), status = VideoResourceStatus.SEARCH_DISABLED))

        bulkUpdate.execute(BulkUpdateRequest(ids = listOf(videoId.value), status = VideoResourceStatus.SEARCHABLE))

        verify(legacySearchService).upsert(argThat { toList().isEmpty() }, isNull())
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdate.execute(null) }
    }
}