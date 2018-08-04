package com.boclips.videoanalyser.domain.service

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.infrastructure.BoclipsVideoRepository
import com.boclips.videoanalyser.infrastructure.kaltura.PagedKalturaMediaService
import com.boclips.videoanalyser.testsupport.TestFactory
import com.nhaarman.mockito_kotlin.any
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
    lateinit var boclipsVideoService: BoclipsVideoRepository

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
        whenever(kalturaMediaService.getPendingMediaEntries()).thenReturn(setOf(
                TestFactory.kalturaVideo(referenceId = "5")))
        whenever(boclipsVideoService.getVideoMetadata(any())).thenAnswer { invocation ->
            val set : Collection<String> = invocation.arguments[0] as Collection<String>
            set.map { BoclipsVideo(id = it.toInt()) }.toSet()
        }
    }

    @Test
    fun getFaultyVideosFromKaltura() {
        whenever(kalturaMediaService.getFaultyMediaEntries()).thenReturn(setOf(
                TestFactory.kalturaVideo(referenceId = "2"),
                TestFactory.kalturaVideo(referenceId = "3")))

        val faultyVideosFromKaltura = videoAnalysisService.getFaultyVideosFromKaltura()

        assertThat(faultyVideosFromKaltura.map { it.id }).contains(2, 3)
    }

    @Test
    fun getNonErrorVideosFromKaltura() {
        whenever(kalturaMediaService.getReadyMediaEntries()).thenReturn(setOf(
                TestFactory.kalturaVideo(referenceId = "2")))

        whenever(kalturaMediaService.getPendingMediaEntries()).thenReturn(setOf(
                TestFactory.kalturaVideo(referenceId = "1")))

        assertThat(videoAnalysisService.getNonErrorVideosFromKaltura().map { it.id }).contains(1, 2)
    }

    @Test
    fun getAllVideosFromBoclips() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = 1),
                TestFactory.boclipsVideo(id = 2)))

        assertThat(videoAnalysisService.getAllVideosFromBoclips().map { it.id }).contains(1, 2)
    }

    @Test
    fun getUnplayableVideos_allBoclipsVideosMinusReadyAndPending() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = 1),
                TestFactory.boclipsVideo(id = 2),
                TestFactory.boclipsVideo(id = 3),
                TestFactory.boclipsVideo(id = 5)))

        assertThat(videoAnalysisService.getUnplayableVideos().map { it.id }).containsExactly(1)
    }

    @Test
    fun returnsDifferenceBetweenKalturaAndBoclips() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = 1),
                TestFactory.boclipsVideo(id = 2)))

        assertThat(videoAnalysisService.getRemovableKalturaVideos().map { it.referenceId }).containsExactly("3", "4")
    }

    @Test
    fun returnsPlayableVideos() {
        whenever(boclipsVideoService.getAllVideos()).thenReturn(setOf(
                TestFactory.boclipsVideo(id = 1),
                TestFactory.boclipsVideo(id = 2)
        ))

        assertThat(videoAnalysisService.getPlayableVideos().map { it.id }).containsExactly(2)
    }
}