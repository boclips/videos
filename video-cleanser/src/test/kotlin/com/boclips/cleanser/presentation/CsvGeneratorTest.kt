package com.boclips.cleanser.presentation

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class CsvGeneratorTest {
    private val csvGenerator = CsvGenerator()
    private val csvMapper = CsvMapper()
    private val csvSchema = csvMapper.schemaFor(BoclipsVideoCsv::class.java)
            .withHeader()
            .withColumnReordering(true)
            .withArrayElementSeparator(",")

    @Test
    fun generateCsvWithIds() {
        val file = File.createTempFile("test.csv", ".tmp")

        csvGenerator.writeCsv(file, setOf(boclipsVideoCsv("1"), boclipsVideoCsv("2")))

        assertThat(extractRows(file)[0].id).isEqualTo("1")
        assertThat(extractRows(file)[1].id).isEqualTo("2")
    }

    private fun boclipsVideoCsv(id: String): BoclipsVideoCsv {
        val boclipsVideoCsv = BoclipsVideoCsv()
        boclipsVideoCsv.id = id
        return boclipsVideoCsv
    }

    private fun extractRows(file: File?): MutableList<BoclipsVideoCsv> {
        val csvRows = csvMapper.readerFor(BoclipsVideoCsv::class.java)
                .with(csvSchema)
                .readValues<BoclipsVideoCsv>(file)

        val allRows = mutableListOf<BoclipsVideoCsv>()
        while (csvRows.hasNext()) {
            val entry = csvRows.next()
            allRows.add(entry)
        }
        return allRows
    }
}
