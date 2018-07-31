package com.boclips.videoanalyser.domain.duplicates.service

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate
import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.infrastructure.boclips.BoclipsVideoRepository
import com.boclips.videoanalyser.testsupport.TestFactory.Companion.boclipsVideo
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DelegatingDuplicateServiceTest {

    @Mock
    lateinit var strategy1: DuplicateStrategy

    @Mock
    lateinit var strategy2: DuplicateStrategy

    @Mock
    lateinit var boclipsVideoRepository: BoclipsVideoRepository

    private val allDuplicateFake = object : DuplicateStrategy{
        override fun findDuplicates(videos: Iterable<BoclipsVideo>) = setOf(Duplicate(
                originalVideo = videos.first(),
                duplicates = videos.toList().subList(1, videos.toList().size)
        ))
    }

    @Test
    fun `delegates down to all strategies`() {
        val videos = setOf(boclipsVideo())
        whenever(boclipsVideoRepository.getAllVideos()).thenReturn(videos)

        DelegatingDuplicateService(setOf(strategy1, strategy2), boclipsVideoRepository).getDuplicates()

        verify(strategy1).findDuplicates(videos)
        verify(strategy2).findDuplicates(videos)
    }

    @Test
    fun `filters duplicates from list for further strategies`() {
        val originalVideo = boclipsVideo()
        val duplicate1 = boclipsVideo()
        val duplicate2 = boclipsVideo()
        val videos = setOf(
                originalVideo,
                duplicate1,
                duplicate2
        )
        whenever(boclipsVideoRepository.getAllVideos()).thenReturn(videos)

        DelegatingDuplicateService(setOf(allDuplicateFake, strategy2), boclipsVideoRepository).getDuplicates()

        verify(strategy2).findDuplicates(setOf(originalVideo))
    }
}