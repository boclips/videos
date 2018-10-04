package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.application.csv.ExpectationsCsvReader
import com.boclips.videoanalyser.application.csv.SearchBenchmarkReportConverter
import com.boclips.videoanalyser.domain.service.search.SearchBenchmarkService
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream

@Service
class RunSearchBenchmark(private val searchBenchmarkService: SearchBenchmarkService) {
    fun execute(inputExpectationsFilePath: String, outputReportFilePath: String) {
        val expectations = FileInputStream(inputExpectationsFilePath).use { input ->
            ExpectationsCsvReader().read(input).mapNotNull { it.toSearchExpectation() }
        }
        val results = searchBenchmarkService.benchmark(expectations)

        File(outputReportFilePath).writeText(SearchBenchmarkReportConverter.convert(results))
    }
}
