package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File

class VideoControllerCsvValidationIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Value("classpath:video_tagging_csvs/invalid_no_video_id_value.csv")
    lateinit var invalidNoVideoIdValue: Resource

    @Value("classpath:video_tagging_csvs/invalid_category_code.csv")
    lateinit var invalidCategoryCodeCsv: Resource

    @Value("classpath:video_tagging_csvs/invalid_no_category_code_column.csv")
    lateinit var noCategoryCodeColumn: Resource

    @Value("classpath:video_tagging_csvs/invalid_data.csv")
    lateinit var invalidData: Resource

    @Value("classpath:video_tagging_csvs/invalid_both_columns_missing.csv")
    lateinit var invalidBothColumnsMissing: Resource

    @Value("classpath:video_tagging_csvs/invalid_no_category_code_column_no_data.csv")
    lateinit var invalidNoCategoryCodeColumnNoData: Resource

    @Value("classpath:video_tagging_csvs/invalid_wrong_columns_and_wrong_data.csv")
    lateinit var invalidWrongColumnsAndWrongData: Resource

    @Value("classpath:video_tagging_csvs/invalid_multiple_errors_in_different_rows.csv")
    lateinit var invalidMultipleErrorsInDifferentRows: Resource

    @Nested
    inner class ValidCsvs {
        @Test
        fun `returns no validation errors when file is valid CSV with proper data`() {
            saveCategory(CategoryFactory.sample(code = "A"))
            saveCategory(CategoryFactory.sample(code = "B"))

            val csvName = "valid-ids.csv"
            val csvFile = File(csvName)
            val saveVideo1Id = saveVideo().value
            val saveVideo2Id = saveVideo().value
            val saveVideo3Id = saveVideo().value

            val header = listOf("ID", "Category Code", "three")
            val row1 = listOf(saveVideo1Id, "A", "three")
            val row2 = listOf(saveVideo2Id, "B", "three")
            val row3 = listOf(saveVideo3Id, "A", "three")

            csvWriter().open(csvName) {
                writeRow(header)
                writeRow(row1)
                writeRow(row2)
                writeRow(row3)
            }

            val fixture = csvFile.inputStream()

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", fixture.readBytes())
                    .asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", equalTo("Data has been successfully imported!")))

            csvFile.delete()
        }

        @Test
        fun `returns no validation errors when file is valid CSV with empty but existing categories column`() {
            saveCategory(CategoryFactory.sample(code = "PST"))

            val csvName = "valid-ids-with-empty-categories.csv"
            val csvFile = File(csvName)
            val saveVideo1Id = saveVideo().value
            val saveVideo2Id = saveVideo().value
            val saveVideo3Id = saveVideo().value

            val header = listOf("ID", "Category Code", "three")
            val row1 = listOf(saveVideo1Id, "", "three")
            val row2 = listOf(saveVideo2Id, "", "three")
            val row3 = listOf(saveVideo3Id, "", "three")

            csvWriter().open(csvName) {
                writeRow(header)
                writeRow(row1)
                writeRow(row2)
                writeRow(row3)
            }

            val fixture = csvFile.inputStream()

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file(
                        "file",
                        fixture.readBytes()
                    )
                    .asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", equalTo("Data has been successfully imported!")))

            csvFile.delete()
        }

        @Test
        fun `tags a video with the categories specified in the csv`() {
            saveCategory(CategoryFactory.sample("A"))

            val videoId = saveVideo(manualCategories = emptyList())
            val csvFile = File.createTempFile("temp", "csv")

            csvFile.printWriter().use { out ->
                out.println("ID,Category Code")
                out.println("${videoId.value},A")
            }
            csvFile.deleteOnExit()

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", csvFile.readBytes())
                    .asBoclipsEmployee()
            )
                .andExpect(status().isOk)

            mockMvc.perform(get("/v1/videos/$videoId").asBoclipsEmployee())
                .andExpect(jsonPath("$.id", equalTo(videoId.value)))
                .andExpect(jsonPath("$.taxonomy.manual.categories", hasSize<Int>(1)))
                .andExpect(jsonPath("$.taxonomy.manual.categories[0].codeValue", equalTo("A")))
        }
    }

    @Nested
    inner class InvalidCsvs {
        @Test
        fun `returns an error about missing columns when both video ID and category code columns are missing`() {
            saveCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidBothColumnsMissing.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error about missing columns when no data rows are given and no category code column is present`() {
            saveCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidNoCategoryCodeColumnNoData.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error about missing columns when columns and values are invalid`() {
            saveCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidWrongColumnsAndWrongData.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error when file have no Category Code column`() {
            saveCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", noCategoryCodeColumn.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error when category code is invalid of unknown`() {
            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidCategoryCodeCsv.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo("Rows 2 contain invalid or unknown category codes - gibberish")
                    )
                )
        }

        @Test
        fun `returns concatenated error messages when multiple rows contain errors`() {
            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidMultipleErrorsInDifferentRows.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo(
                            "Rows 2, 3, 4 contain invalid or unknown category codes - A, INVALID"
                        )
                    )
                )
        }
        @Test
        fun `applies category codes to videos that have valid video ids`() {
            saveCategory(CategoryFactory.sample(code = "A"))
            saveCategory(CategoryFactory.sample(code = "B"))

            val csvName = "videos.csv"
            val csvFile = File(csvName)
            val saveVideo1Id = saveVideo().value
            val saveVideo2Id = saveVideo().value

            val header = listOf("ID", "Category Code", "three")
            val row1 = listOf("", "A", "three")
            val row2 = listOf(saveVideo1Id, "B", "three")
            val row3 = listOf(saveVideo2Id, "A", "three")
            val row4 = listOf("", "A", "three")

            csvWriter().open(csvName) {
                writeRow(header)
                writeRow(row1)
                writeRow(row2)
                writeRow(row3)
                writeRow(row4)
            }

            val fixture = csvFile.inputStream()

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", fixture.readBytes())
                    .asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", equalTo("Rows 2, 5 have not been applied because of a missing video ID")))

            csvFile.delete()
        }
    }
}
