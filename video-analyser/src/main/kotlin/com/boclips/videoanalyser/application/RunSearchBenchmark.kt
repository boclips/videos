package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.application.csv.ExpectationsCsvReader
import com.boclips.videoanalyser.domain.service.search.SearchBenchmarkService
import org.springframework.stereotype.Service
import java.io.FileInputStream

@Service
class RunSearchBenchmark(private val searchBenchmarkService: SearchBenchmarkService) {
    fun runSearchBenchmark(filename: String): String {
        val expectations = FileInputStream(filename).use { input ->
            ExpectationsCsvReader().read(input).map { it.toSearchExpectation() }
        }
        return searchBenchmarkService.benchmark(expectations).toString()
    }
}
