package com.boclips.videoanalyser.domain.service.search

import com.boclips.videoanalyser.domain.model.search.SearchBenchmarkReport
import com.boclips.videoanalyser.domain.model.search.SearchExpectation

class SearchBenchmarkService(private val searchClient: SearchClient) {

    fun benchmark(expectations: Iterable<SearchExpectation>): SearchBenchmarkReport {
        return expectations.fold(SearchBenchmarkReport(0, 0)) { report, expectation ->
            val hit = searchClient.searchTop10(expectation.query).contains(expectation.videoId)
            println("${if (hit) "HIT: " else "MISS:"} query='${expectation.query}' video=${expectation.videoId}")
            report.copy(
                    total = report.total + 1,
                    hits = report.hits + if (hit) 1 else 0
            )
        }
    }

}
