package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.application.BoclipsVideoCsv
import com.boclips.videoanalyser.application.CsvGenerator
import com.boclips.videoanalyser.application.BoclipsVideoCsv.Companion.CONTENT_PROVIDER
import com.boclips.videoanalyser.application.BoclipsVideoCsv.Companion.CONTENT_PROVIDER_ID
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    fun writeCsv() {
        val file = File.createTempFile("test.csv", ".tmp")

        csvGenerator.writeCsv(file, setOf(
                BoclipsVideoCsv(id = "1", referenceId = "ref-1", title = "t1", description = "d1", provider = "p1", providerId = "pid1", duration = "01:02:03", date = LocalDateTime.of(2018, Month.DECEMBER, 1, 2, 3, 4)),
                BoclipsVideoCsv(id = "2")
        ), BoclipsVideoCsv.ALL_COLUMNS)

        assertThat(parseCsv(file)[0]["Id"]).isEqualTo("1")
        assertThat(parseCsv(file)[0]["Reference Id"]).isEqualTo("ref-1")
        assertThat(parseCsv(file)[0]["Title"]).isEqualTo("t1")
        assertThat(parseCsv(file)[0]["Content Provider"]).isEqualTo("p1")
        assertThat(parseCsv(file)[0]["Content Provider Id"]).isEqualTo("pid1")
        assertThat(parseCsv(file)[0]["Description"]).isEqualTo("d1")
        assertThat(parseCsv(file)[0]["Date"]).isEqualTo("2018-12-01T02:03:04")
        assertThat(parseCsv(file)[0]["Duration"]).isEqualTo("01:02:03")

        assertThat(parseCsv(file)[1]["Id"]).isEqualTo("2")
    }

    @Test
    fun writeCsv_whenColumnsAreSpecified() {
        val file = File.createTempFile("test.csv", ".tmp")

        csvGenerator.writeCsv(file, setOf(
                BoclipsVideoCsv(id = "1", referenceId = "ref-1", title = "t1", description = "d1", provider = "p1", providerId = "pid1", duration = "01:02:03", date = LocalDateTime.of(2018, Month.DECEMBER, 1, 2, 3, 4))
        ), setOf(CONTENT_PROVIDER, CONTENT_PROVIDER_ID))

        assertThat(parseCsv(file)[0]["Id"]).isNull()
        assertThat(parseCsv(file)[0]["Reference Id"]).isNull()
        assertThat(parseCsv(file)[0]["Title"]).isNull()
        assertThat(parseCsv(file)[0]["Content Provider"]).isNotBlank()
        assertThat(parseCsv(file)[0]["Content Provider Id"]).isNotBlank()
        assertThat(parseCsv(file)[0]["Description"]).isNull()
        assertThat(parseCsv(file)[0]["Date"]).isNull()
        assertThat(parseCsv(file)[0]["Duration"]).isNull()
    }

    @Test
    fun writeCsv_whenInvalidColumnsAreSpecified() {
        val file = File.createTempFile("test.csv", ".tmp")

        assertThatThrownBy {
            csvGenerator.writeCsv(file, setOf(BoclipsVideoCsv(id = "1")), setOf(CONTENT_PROVIDER, "not a column name"))
        }
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
