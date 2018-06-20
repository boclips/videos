package com.boclips.cleanser.infrastructure

import com.boclips.cleanser.domain.service.VideoAnalysisService
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.PagedKalturaMediaService
import com.boclips.testsupport.TestFactory
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
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

    @Before
    fun setUp() {
        whenever(kalturaMediaService.getReadyMediaEntries()).thenReturn(setOf(
                TestFactory.kalturaVideo(referenceId = "2"),
                TestFactory.kalturaVideo(referenceId = "3"),
                TestFactory.kalturaVideo(referenceId = "4")))
    }

    @Test
    fun returnsDifferenceBetweenBoAndKaltura() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = "1"),
                TestFactory.boclipsVideo(id = "2"),
                TestFactory.boclipsVideo(id = "3")))

        assertThat(videoAnalysisService.getUnplayableVideos()).containsExactly("1")
    }

    @Test
    fun returnsDifferenceBetweenKalturaAndBoclips() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = "1"),
                TestFactory.boclipsVideo(id = "2")))

        assertThat(videoAnalysisService.getFreeableVideos()).containsExactly("3", "4")
    }

    @Test
    fun returnsPlayableVideos() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = "1"),
                TestFactory.boclipsVideo(id = "2")
        ))

        assertThat(videoAnalysisService.getPlayableVideos()).containsExactly("2")
    }
}