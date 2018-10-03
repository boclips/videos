package com.boclips.videoanalyser.application.csv

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExpectationsCsvReaderTest {

    @Test
    fun read() {
        val csvLines = ExpectationsCsvReader().read(csvContent.byteInputStream())

        assertThat(csvLines).hasSize(2)
        assertThat(csvLines.first()).isEqualTo(SearchExpectationCsv("linear equations", "http://boclips.com/video/2352831"))
        assertThat(csvLines.last()).isEqualTo(SearchExpectationCsv("Enzyme", "https://boclips.com/video/2538137"))
    }

    private val csvContent = """
        QUERY,VIDEO,COLLECTION
        linear equations,http://boclips.com/video/2352831,
        Enzyme,https://boclips.com/video/2538137,
    """.trimIndent()
}
