package com.boclips.videoanalyser.application.csv

import com.boclips.videoanalyser.application.csv.BoclipsVideoCsv.Companion.ALL_COLUMNS
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File

open class CsvGenerator {
    private val mapper = CsvMapper().apply {
        findAndRegisterModules()
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    }

    fun writeCsv(file: File, videos: Collection<BoclipsVideoCsv>, columns: Set<String>) {

        if ((columns - ALL_COLUMNS).isNotEmpty()) {
            throw IllegalArgumentException("Invalid column(s): ${(columns - ALL_COLUMNS).joinToString()}")
        }

        val allColumnsSchema = mapper.schemaFor(BoclipsVideoCsv::class.java)
                .withHeader()
                .withColumnReordering(true)
                .withArrayElementSeparator(",")

        val schema = CsvSchema.Builder(allColumnsSchema)
                .clearColumns()
                .addColumns(allColumnsSchema.filter { columns.contains(it.name) })

                .build()

        val fileContent = mapper.writer(schema).writeValueAsBytes(videos)

        file.writeBytes(fileContent)
    }

}

