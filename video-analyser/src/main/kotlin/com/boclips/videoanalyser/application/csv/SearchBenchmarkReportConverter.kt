package com.boclips.videoanalyser.application.csv

import com.boclips.videoanalyser.domain.model.search.SearchBenchmarkReportItem
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper

object SearchBenchmarkReportConverter {

    fun convert(items: List<SearchBenchmarkReportItem>): String {

        val lines = items.map { SearchBenchmarkReportItemCsv(
                query = it.expectation.query,
                video = it.expectation.videoId,
                legacySearchHit = it.legacySearchHit.toString(),
                videoServiceHit = it.videoServiceHit.toString()
        ) }

        val schema = mapper.schemaFor(SearchBenchmarkReportItemCsv::class.java)
                .withHeader()
                .withArrayElementSeparator(",")

        return mapper.writer(schema).writeValueAsString(lines)
    }

    private val mapper = CsvMapper().apply {
        findAndRegisterModules()
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    }

}