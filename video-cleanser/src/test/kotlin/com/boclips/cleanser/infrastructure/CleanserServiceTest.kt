package com.boclips.cleanser.infrastructure

import com.boclips.cleanser.domain.model.MediaItem
import com.boclips.cleanser.domain.service.CleanserService
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.TimePagedKalturaMediaService
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CleanserServiceTest {
    @Mock
    lateinit var boclipsVideosRepo: BoclipsVideosRepository

    @Mock
    lateinit var timePagedKalturaVideosRepo: TimePagedKalturaMediaService

    @InjectMocks
    lateinit var cleanserService: CleanserService

    @Test
    fun returnsDifferenceBetweenBoAndKaltura() {
        whenever(boclipsVideosRepo.getAllPublishedVideos()).thenReturn(setOf("1", "2", "3"))
        whenever(timePagedKalturaVideosRepo.getReadyMediaEntries()).thenReturn(
                setOf(
                        MediaItem(referenceId = "2"),
                        MediaItem(referenceId = "3"),
                        MediaItem(referenceId = "4")
                )
        )

        assertThat(cleanserService.getUnplayableVideos()).containsExactly("1")
    }
}