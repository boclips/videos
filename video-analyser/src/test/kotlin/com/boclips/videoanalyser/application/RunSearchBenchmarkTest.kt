package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.domain.model.search.SearchBenchmarkReport
import com.boclips.videoanalyser.domain.service.search.SearchBenchmarkService
import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.File


class RunSearchBenchmarkTest {

    private var dataset: File? = null

    @Before
    fun setUp() {
        dataset = createTempFile(suffix = "csv")
    }

    @After
    fun tearDown() {
        dataset?.deleteOnExit()
    }

    @Test
    fun runSearchBenchmark() {

        dataset?.writeText(datasetFileContent)

        val searchBenchmarkService = mock(SearchBenchmarkService::class.java)
        whenever(searchBenchmarkService.benchmark(any())).thenReturn(SearchBenchmarkReport(total = 10, hits = 5))

        val runSearchBenchmark = RunSearchBenchmark(searchBenchmarkService)

        runSearchBenchmark.runSearchBenchmark(dataset!!.absolutePath)

        verify(searchBenchmarkService).benchmark(listOf(
                SearchExpectation("linear equations", "2352831")
        ))
    }

    private val datasetFileContent = """
        QUERY,VIDEO
        linear equations,2352831
    """.trimIndent()

}
