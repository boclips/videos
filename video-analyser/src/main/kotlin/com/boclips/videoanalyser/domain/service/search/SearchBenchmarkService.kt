package com.boclips.videoanalyser.domain.service.search

import com.boclips.videoanalyser.domain.model.search.SearchBenchmarkReportItem
import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import com.boclips.videoanalyser.infrastructure.search.LegacyBoclipsSearchClient
import com.boclips.videoanalyser.infrastructure.search.VideoServiceSearchClient

class SearchBenchmarkService(
        private val legacySearchClient: LegacyBoclipsSearchClient,
        private val videoServiceSearchClient: VideoServiceSearchClient
) {
    fun benchmark(expectations: Iterable<SearchExpectation>): List<SearchBenchmarkReportItem> {
        return expectations.map { expectation ->

            SearchBenchmarkReportItem(
                    expectation = expectation,
                    legacySearchHit = check(expectation, legacySearchClient),
                    videoServiceHit = check(expectation, videoServiceSearchClient)
            )
        }
    }

    private fun check(expectation: SearchExpectation, searchClient: SearchClient): Boolean {
        return searchClient.searchTop10(expectation.query).contains(expectation.videoId).apply {
            println("[${searchClient.serviceName()}] ${if (this) "HIT: " else "MISS:"} query='${expectation.query}' video=${expectation.videoId}")
        }
    }

}
