package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.CustomMetadataValidated
import com.boclips.videos.service.presentation.DataRowsContainMetadataErrors
import com.boclips.videos.service.presentation.MissingColumn
import com.boclips.videos.service.presentation.VideoIdMetadataDoesntExist
import com.boclips.videos.service.presentation.converters.customMetadata.CustomMetadataFileValidator
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import java.io.File

class CustomMetadataFileValidatorTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var customMetadataFileValidator: CustomMetadataFileValidator

    @Test
    fun `returns valid csv`() {
        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)
        val saveVideo1Id = saveVideo().value

        val header = listOf("ID", "Topic ID", "Topic")
        val row1 = listOf(saveVideo1Id, "", "")

        csvWriter().open(csvName) {
            writeRow(header)
            writeRow(row1)
        }

        val fixture = InputStreamResource(csvFile.inputStream())

        val result = customMetadataFileValidator.validate(fixture)

        assertThat(result).isInstanceOf(CustomMetadataValidated::class.java)
        assertThat((result as CustomMetadataValidated).entries).hasSize(1)
    }

    @Test
    fun `returns error when columns are invalid`() {
        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)

        val header = listOf("ID", "Missing Topic Id", "Missing Topic")
        val row1 = listOf("wrong-video-id", "", "")

        csvWriter().open(csvName) {
            writeRow(header)
            writeRow(row1)
        }

        val fixture = InputStreamResource(csvFile.inputStream())

        val result = customMetadataFileValidator.validate(fixture)
        assertThat(result).isInstanceOf(MissingColumn::class.java)
    }

    @Test
    fun `returns error when video id is not valid`() {
        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)

        val header = listOf("ID", "Topic", "Topic ID")
        val row1 = listOf("wrong-video-id", "", "")

        csvWriter().open(csvName) {
            writeRow(header)
            writeRow(row1)
        }

        val fixture = InputStreamResource(csvFile.inputStream())

        val result = customMetadataFileValidator.validate(fixture)
        assertThat(result).isInstanceOf(DataRowsContainMetadataErrors::class.java)
        assertThat((result as DataRowsContainMetadataErrors).errors).hasSize(1)
        assertThat(result.errors.first()).isInstanceOf(
            VideoIdMetadataDoesntExist::class.java
        )
        assertThat(result.getMessage()).isEqualTo(
            "Rows 2 contains invalid video ID"
        )
    }

    @Test
    fun `returns no entries when no rows present`() {
        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)

        val header = listOf("ID", "Topic", "Topic ID")

        csvWriter().open(csvName) {
            writeRow(header)
        }

        val fixture = InputStreamResource(csvFile.inputStream())

        val result = customMetadataFileValidator.validate(fixture)
        assertThat(result).isInstanceOf(CustomMetadataValidated::class.java)
        assertThat((result as CustomMetadataValidated).entries).hasSize(0)
    }
}
