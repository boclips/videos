package com.boclips.videoanalyser.domain.service

import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class SearchBenchmarkServiceTest{

    @InjectMocks
    lateinit var searchBenchmarkService: SearchBenchmarkService

    @Mock
    lateinit var searchClient: SearchClient

    @Test
    fun whenHit_returnsReport() {
        whenever(searchClient.searchTop10("enzyme")).thenReturn(listOf("123"))

        val (total, hits) = searchBenchmarkService.benchmark(listOf(SearchExpectation("enzyme", "123")))

        assertThat(hits).isEqualTo(1)
        assertThat(total).isEqualTo(1)
    }
    @Test
    fun whenMultipleExpectations_returnsReport() {
        whenever(searchClient.searchTop10("enzyme")).thenReturn(listOf("123"))
        whenever(searchClient.searchTop10("dog")).thenReturn(listOf("333"))

        val (total, hits) = searchBenchmarkService.benchmark(listOf(SearchExpectation("enzyme", "123"),SearchExpectation("dog", "333")))

        assertThat(hits).isEqualTo(2)
        assertThat(total).isEqualTo(2)
    }

    @Test
    fun whenMiss_returnsReport() {
        whenever(searchClient.searchTop10("enzyme")).thenReturn(listOf("456", "789"))

        val (total, hits) = searchBenchmarkService.benchmark(listOf(SearchExpectation("enzyme", "123")))

        assertThat(hits).isEqualTo(0)
        assertThat(total).isEqualTo(1)
    }
}

