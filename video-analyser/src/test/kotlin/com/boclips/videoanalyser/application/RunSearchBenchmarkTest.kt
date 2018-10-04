package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.domain.model.search.SearchBenchmarkReportItem
import com.boclips.videoanalyser.domain.service.search.SearchBenchmarkService
import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.File


class RunSearchBenchmarkTest {

    private lateinit var inputDatasetFile: File

    private lateinit var outputReportFile: File

    private lateinit var searchBenchmarkService: SearchBenchmarkService

    @Before
    fun setUp() {
        inputDatasetFile = createTempFile(suffix = "csv")
        outputReportFile = createTempFile(suffix = "csv")

        inputDatasetFile.writeText("QUERY,VIDEO\nlinear equations,2352831\n")

        searchBenchmarkService = mock(SearchBenchmarkService::class.java)
    }

    @After
    fun tearDown() {
        inputDatasetFile.deleteOnExit()
        outputReportFile.deleteOnExit()
    }

    @Test
    fun `runSearchBenchmark passes data to the service`() {
        val expectation = SearchExpectation("linear equations", "2352831")
        whenever(searchBenchmarkService.benchmark(any())).thenReturn(listOf(SearchBenchmarkReportItem(expectation, true, true)))

        RunSearchBenchmark(searchBenchmarkService).execute(inputDatasetFile.absolutePath, outputReportFile.absolutePath)

        verify(searchBenchmarkService).benchmark(listOf(
                expectation
        ))
    }

    @Test
    fun `runSearchBenchmark saves results in a file`() {
        val expectation = SearchExpectation("dogs", "2352831")

        whenever(searchBenchmarkService.benchmark(any())).thenReturn(listOf(SearchBenchmarkReportItem(expectation, true, true)))

        RunSearchBenchmark(searchBenchmarkService).execute(inputDatasetFile.absolutePath, outputReportFile.absolutePath)

        assertThat(outputReportFile.readText()).startsWith("Query,Video,boclips.com,video-service\ndogs")
    }

}
