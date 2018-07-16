package com.boclips.videoanalyser.domain.service

import org.springframework.stereotype.Service

data class SearchExpectation(val query: String, val videoId: String)

data class SearchBenchmarkReport(val total: Int, val hits: Int)

@Service
class SearchBenchmarkService(private val searchClient: SearchClient) {

    fun benchmark(expectations: Iterable<SearchExpectation>): SearchBenchmarkReport {

        return expectations.fold(SearchBenchmarkReport(0, 0)) { report, expectation ->
            val hit = searchClient.searchTop10(expectation.query).contains(expectation.videoId)
            report.copy(
                    total = report.total + 1,
                    hits = report.hits + if (hit) 1 else 0
            )
        }
    }

}