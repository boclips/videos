package com.boclips.videoanalyser.application.csv

import com.boclips.videoanalyser.domain.model.search.SearchBenchmarkReportItem
import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SearchBenchmarkReportConverterTest {

    @Test
    fun convert() {
        val item = SearchBenchmarkReportItem(
                expectation = SearchExpectation(query = "the meaning of life", videoId = "1234"),
                legacySearchHit = true,
                videoServiceHit = false
        )

        val csv = SearchBenchmarkReportConverter.convert(listOf(item))

        assertThat(csv).isEqualTo("Query,Video,boclips.com,video-service\n" +
                "\"the meaning of life\",1234,true,false\n")
    }
}