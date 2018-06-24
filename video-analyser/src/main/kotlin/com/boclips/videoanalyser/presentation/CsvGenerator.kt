package com.boclips.videoanalyser.presentation

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.File

open class CsvGenerator {
    private val mapper = CsvMapper()

    fun writeCsv(file: File, videos: Collection<BoclipsVideoCsv>) {
        val csvSchema = mapper.schemaFor(BoclipsVideoCsv::class.java)
                .withHeader()
                .withColumnReordering(true)
                .withArrayElementSeparator(",")

        val fileContent = mapper.writer(csvSchema).writeValueAsBytes(videos)

        file.writeBytes(fileContent)
    }

}

