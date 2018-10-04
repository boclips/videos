package com.boclips.videoanalyser.domain.service.search

import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import com.boclips.videoanalyser.infrastructure.search.LegacyBoclipsSearchClient
import com.boclips.videoanalyser.infrastructure.search.VideoServiceSearchClient
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SearchBenchmarkServiceTest {

    @InjectMocks
    lateinit var searchBenchmarkService: SearchBenchmarkService

    @Mock
    lateinit var legacySearchClient: LegacyBoclipsSearchClient

    @Mock
    lateinit var videoServiceSearchClient: VideoServiceSearchClient

    @Test
    fun `benchmark returns report items`() {
        whenever(legacySearchClient.searchTop10(any())).thenReturn(listOf("333"))
        whenever(videoServiceSearchClient.searchTop10(any())).thenReturn(listOf("123"))

        val (item1, item2) = searchBenchmarkService.benchmark(listOf(SearchExpectation("enzyme", "123"), SearchExpectation("dog", "333")))

        assertThat(item1.legacySearchHit).isFalse()
        assertThat(item1.videoServiceHit).isTrue()
        assertThat(item2.legacySearchHit).isTrue()
        assertThat(item2.videoServiceHit).isFalse()
    }
}

