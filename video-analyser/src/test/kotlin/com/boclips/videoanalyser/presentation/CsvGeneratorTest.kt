package com.boclips.videoanalyser.presentation

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import java.time.Month

class CsvGeneratorTest {
    private val csvGenerator = CsvGenerator()
    private val csvMapper = CsvMapper()
    private val csvSchema = csvMapper.schemaFor(Map::class.java)
            .withHeader()
            .withColumnReordering(true)
            .withArrayElementSeparator(",")

    @Test
    fun generateCsvWithIds() {
        val file = File.createTempFile("test.csv", ".tmp")

        csvGenerator.writeCsv(file, setOf(
                BoclipsVideoCsv(id = "1", referenceId = "ref-1", title = "t1", description = "d1", provider = "p1", duration = "01:02:03", date = LocalDateTime.of(2018, Month.DECEMBER, 1, 2, 3, 4)),
                BoclipsVideoCsv(id = "2")
        ))

        assertThat(parseCsv(file)[0]["Id"]).isEqualTo("1")
        assertThat(parseCsv(file)[0]["Reference Id"]).isEqualTo("ref-1")
        assertThat(parseCsv(file)[0]["Title"]).isEqualTo("t1")
        assertThat(parseCsv(file)[0]["Content Provider"]).isEqualTo("p1")
        assertThat(parseCsv(file)[0]["Description"]).isEqualTo("d1")
        assertThat(parseCsv(file)[0]["Date"]).isEqualTo("2018-12-01T02:03:04")
        assertThat(parseCsv(file)[0]["Duration"]).isEqualTo("01:02:03")

        assertThat(parseCsv(file)[1]["Id"]).isEqualTo("2")
    }

    private fun parseCsv(file: File?): List<Map<String, String>> {

        val csvRows = csvMapper.readerFor(Map::class.java)
                .with(csvSchema)
                .readValues<Map<String, String>>(file)

        val allRows = mutableListOf<Map<String, String>>()
        while (csvRows.hasNext()) {
            val entry = csvRows.next()
            allRows.add(entry)
        }
        return allRows
    }
}
