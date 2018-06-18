package com.boclips.cleanser.infrastructure

import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.KalturaVideosRepository
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CleanserServiceImplTest {
    @Mock
    lateinit var boclipsVideosRepo: BoclipsVideosRepository

    @Mock
    lateinit var kalturaVideosRepo: KalturaVideosRepository

    @InjectMocks
    lateinit var cleanserService: CleanserServiceImpl

    @Test
    fun returnsDifferenceBetweenBoAndKaltura() {
        whenever(boclipsVideosRepo.getAllLegacyIds()).thenReturn(setOf(1, 2, 3))
        whenever(kalturaVideosRepo.getAllIds()).thenReturn(setOf("2", "3", "4"))

        assertThat(cleanserService.getNonPlayableVideos()).containsExactly("1")
    }
}