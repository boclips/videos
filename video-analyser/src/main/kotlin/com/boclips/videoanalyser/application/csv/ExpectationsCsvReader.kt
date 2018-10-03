package com.boclips.videoanalyser.application.csv

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.InputStream


class ExpectationsCsvReader {
    fun read(input: InputStream): Iterable<SearchExpectationCsv> {
        val mapper = CsvMapper()
        mapper.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE)

        val schema = CsvSchema.builder()
                .setUseHeader(true)
                .addColumn("QUERY")
                .addColumn("VIDEO")
                .build()

        return mapper.readerFor(SearchExpectationCsv::class.java).with(schema)
                .readValues<SearchExpectationCsv>(input)
                .readAll()
    }
}
