package com.boclips.cleanser

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
    lateinit var boclipsVideosRepo : BoclipsVideosRepository

    @Mock
    lateinit var kalturaVideosRepo : KalturaVideosRepository

    @InjectMocks
    lateinit var service : CleanserService

    @Test
    fun returnsDifferenceBetweenBoAndKaltura() {
        whenever(boclipsVideosRepo.getAllIds()).thenReturn(setOf(1,2,3))
        whenever(kalturaVideosRepo.getAllNonErroredVideos()).thenReturn(setOf(2,3,4))

        assertThat(service.getNonPlayableVideos()).containsExactly(1)
    }
}