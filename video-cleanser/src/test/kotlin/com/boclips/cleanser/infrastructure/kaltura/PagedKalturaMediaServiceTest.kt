package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.domain.model.MediaFilterType
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito


class PagedKalturaMediaServiceTest {
    val mockKalturaClient = Mockito.mock(KalturaMediaClient::class.java)
    val mockPagingationOrchestrator = Mockito.mock(PaginationOrchestrator::class.java)

    @Test
    fun countAllMediaEntries() {
        val kalturaMediaService = PagedKalturaMediaService(mockKalturaClient, mockPagingationOrchestrator)

        kalturaMediaService.countAllMediaEntries()

        verify(mockKalturaClient, times(2)).count(check {
            assertThat(extractFilters(it))
                    .containsAnyElementsOf(listOf(MediaFilterType.STATUS_IN, MediaFilterType.STATUS_NOT_EQUAL))
        })
    }

    @Test
    fun getReadyMediaEntries() {
        val kalturaMediaService = PagedKalturaMediaService(mockKalturaClient, mockPagingationOrchestrator)

        kalturaMediaService.getReadyMediaEntries()

        verify(mockPagingationOrchestrator, times(1)).fetchAll(check {
            val mediaFilter = it[0]
            assertThat(mediaFilter.key).isEqualTo(MediaFilterType.STATUS_IN)
            assertThat(mediaFilter.value).isEqualTo("2")
        })
    }

    @Test
    fun getFaultyMediaEntries() {
        val kalturaMediaService = PagedKalturaMediaService(mockKalturaClient, mockPagingationOrchestrator)

        kalturaMediaService.getFaultyMediaEntries()

        verify(mockPagingationOrchestrator, times(1)).fetchAll(check {
            val mediaFilter = it[0]
            assertThat(mediaFilter.key).isEqualTo(MediaFilterType.STATUS_NOT_EQUAL)
            assertThat(mediaFilter.value).isEqualTo("2")
        })
    }

    private fun extractFilters(it: List<MediaFilter>) = it.map { it.key }
}
