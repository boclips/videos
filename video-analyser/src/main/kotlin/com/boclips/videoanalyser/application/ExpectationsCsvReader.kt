package com.boclips.videoanalyser.application

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import com.fasterxml.jackson.dataformat.csv.CsvSchema



class ExpectationsCsvReader {

    fun read(input: InputStream): Iterable<SearchExpectationCsv> {
        val mapper = CsvMapper()
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
