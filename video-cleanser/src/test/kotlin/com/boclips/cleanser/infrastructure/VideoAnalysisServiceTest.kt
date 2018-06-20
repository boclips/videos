package com.boclips.cleanser.infrastructure

import com.boclips.cleanser.domain.service.VideoAnalysisService
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.PagedKalturaMediaService
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VideoAnalysisServiceTest {
    @Mock
    lateinit var boclipsVideoService: BoclipsVideosRepository

    @Mock
    lateinit var kalturaMediaService: PagedKalturaMediaService

    @InjectMocks
    lateinit var videoAnalysisService: VideoAnalysisService

    @Test
    fun returnsDifferenceBetweenBoAndKaltura() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf("1", "2", "3"))
        whenever(kalturaMediaService.getReadyMediaEntries()).thenReturn(setOf("2", "3", "4"))

        assertThat(videoAnalysisService.getUnplayableVideos()).containsExactly("1")
    }

    @Test
    fun returnsDifferenceBetweenKalturaAndBoclips() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf("1", "2"))
        whenever(kalturaMediaService.getReadyMediaEntries()).thenReturn(setOf("2", "3", "4"))

        assertThat(videoAnalysisService.getFreeableVideos()).containsExactly("3", "4")
    }

    @Test
    fun returnsPlayableVideos() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf("1", "2"))
        whenever(kalturaMediaService.getReadyMediaEntries()).thenReturn(setOf("2", "3", "4"))

        assertThat(videoAnalysisService.getPlayableVideos()).containsExactly("2")
    }
}