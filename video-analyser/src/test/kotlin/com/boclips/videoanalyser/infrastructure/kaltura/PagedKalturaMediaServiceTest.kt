package com.boclips.videoanalyser.infrastructure.kaltura

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.videoanalyser.domain.model.KalturaVideo
import com.boclips.videoanalyser.domain.model.MediaFilter
import com.boclips.videoanalyser.domain.model.MediaFilterType
import com.boclips.videoanalyser.infrastructure.kaltura.client.KalturaMediaClient
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito


class PagedKalturaMediaServiceTest {
    private val kalturaClient = TestKalturaClient()
    private val mockKalturaClient = Mockito.mock(KalturaMediaClient::class.java)
    private val mockPagingationOrchestrator = Mockito.mock(PaginationOrchestrator::class.java)
    private val kalturaMediaService = PagedKalturaMediaService(mockKalturaClient, mockPagingationOrchestrator, kalturaClient)

    @Test
    fun countAllMediaEntries_passesOnCorrectFilters() {
        kalturaMediaService.countAllMediaEntries()

        verify(mockKalturaClient, times(2)).count(check {
            assertThat(extractFilters(it))
                    .containsAnyElementsOf(listOf(MediaFilterType.STATUS_IN, MediaFilterType.STATUS_NOT_IN))
        })
    }

    @Test
    fun getReadyMediaEntries_passesOnCorrectFilters() {
        kalturaMediaService.getReadyMediaEntries()

        verify(mockPagingationOrchestrator, times(1)).fetchAll(check {
            val mediaFilter = it[0]
            assertThat(mediaFilter.key).isEqualTo(MediaFilterType.STATUS_IN)
            assertThat(mediaFilter.value).isEqualTo("2")
        })
    }

    @Test
    fun getReadyMediaEntries_filtersOutMediaItemWithNullReferenceId() {
        whenever(mockPagingationOrchestrator.fetchAll(anyList())).thenReturn(listOf(MediaItem(referenceId = null, id = "0", downloadUrl = "something.com/download")))

        val readyMediaEntries = kalturaMediaService.getReadyMediaEntries()

        assertThat(readyMediaEntries).hasSize(0)
    }

    @Test
    fun getPendingMediaEntries_passesOnCorrectFilters() {
        kalturaMediaService.getPendingMediaEntries()

        verify(mockPagingationOrchestrator, times(1)).fetchAll(check {
            val mediaFilter = it[0]
            assertThat(mediaFilter.key).isEqualTo(MediaFilterType.STATUS_IN)
            assertThat(mediaFilter.value).isEqualTo("4")
        })
    }

    @Test
    fun getPendingMediaEntries_filtersOutMediaItemWithNullReferenceIc() {
        whenever(mockPagingationOrchestrator.fetchAll(anyList())).thenReturn(listOf(MediaItem(referenceId = null, id = "0", downloadUrl = "something.com/download")))

        val readyMediaEntries = kalturaMediaService.getPendingMediaEntries()

        assertThat(readyMediaEntries).hasSize(0)
    }

    @Test
    fun getReadyMediaEntries_returnsKalturaVideos() {
        whenever(mockPagingationOrchestrator.fetchAll(anyList())).thenReturn(listOf(MediaItem(referenceId = "9", id = "0", downloadUrl = "something.com/url")))

        val kalturaVideos = kalturaMediaService.getReadyMediaEntries()

        assertThat(kalturaVideos.first().referenceId).isEqualTo("9")
        assertThat(kalturaVideos.first().id).isEqualTo("0")
        assertThat(kalturaVideos.first().downloadUrl).isEqualTo("something.com/url")
    }

    @Test
    fun getFaultyMediaEntries_passesOnCorrectFilters() {
        kalturaMediaService.getFaultyMediaEntries()

        verify(mockPagingationOrchestrator, times(1)).fetchAll(check {
            val mediaFilter = it[0]
            assertThat(mediaFilter.key).isEqualTo(MediaFilterType.STATUS_NOT_IN)
            assertThat(mediaFilter.value).isEqualTo("2,4")
        })
    }

    @Test
    fun getFaultyMediaEntries_returnsKalturaVideos() {
        whenever(mockPagingationOrchestrator.fetchAll(anyList())).thenReturn(listOf(MediaItem(referenceId = "9", id = "0", downloadUrl = "something.com/url")))

        val kalturaVideos = kalturaMediaService.getFaultyMediaEntries()

        assertThat(kalturaVideos.first().referenceId).isEqualTo("9")
        assertThat(kalturaVideos.first().id).isEqualTo("0")
        assertThat(kalturaVideos.first().downloadUrl).isEqualTo("something.com/url")
    }

    @Test
    fun getFaultyMediaEntries_filtersOutNullReferenceId() {
        whenever(mockPagingationOrchestrator.fetchAll(anyList())).thenReturn(listOf(MediaItem(referenceId = null, id = "0", downloadUrl = "something.com/url")))

        val kalturaVideos = kalturaMediaService.getFaultyMediaEntries()

        assertThat(kalturaVideos).hasSize(0)
    }

    @Test
    fun removeKalturaVideos() {
        kalturaClient.addMediaEntry(MediaEntry.builder().id("1").referenceId("1").build())

        val videoToBeRemoved = KalturaVideo(referenceId = "1", id = "1", downloadUrl = "")
        val videosToBeRemoved = setOf(videoToBeRemoved)
        val removedMediaEntries = kalturaMediaService.removeMediaEntries(videosToBeRemoved)

        assertThat(removedMediaEntries).hasSize(1)
        assertThat(removedMediaEntries.first()).isEqualTo(videoToBeRemoved)
        assertThat(kalturaClient.getMediaEntriesByReferenceId("1")).hasSize(0)
    }

    private fun extractFilters(it: List<MediaFilter>) = it.map { it.key }
}
