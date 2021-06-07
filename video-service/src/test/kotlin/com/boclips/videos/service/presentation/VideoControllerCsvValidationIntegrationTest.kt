package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.asBoclipsEmployee
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

    @Value("classpath:video_tagging_csvs/valid.csv")
    lateinit var validCategoryCsv: Resource

    @Value("classpath:video_tagging_csvs/valid_columns_empty_categories.csv")
    lateinit var validColumnsEmptyCategories: Resource

    @Value("classpath:video_tagging_csvs/image.csv")
    lateinit var imageFile: Resource

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
            addCategory(CategoryFactory.sample(code = "A"))
            addCategory(CategoryFactory.sample(code = "B"))
            addCategory(CategoryFactory.sample(code = "C"))
            addCategory(CategoryFactory.sample(code = "D"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", validCategoryCsv.file.readBytes())
                    .asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", equalTo("Data has been successfully imported!")))
        }

        @Test
        fun `returns no validation errors when file is valid CSV with empty but existing categories column`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", validColumnsEmptyCategories.file.readBytes())
                    .asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", equalTo("Data has been successfully imported!")))
        }

        @Test
        fun `tags a video with the categories specified in the csv`() {
            addCategory(CategoryFactory.sample("A"))

            val videoId = saveVideo(categories = emptyList())
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

            mockMvc.perform(get("/v1/videos/${videoId}").asBoclipsEmployee())
                .andExpect(jsonPath("$.id", equalTo(videoId.value)))
                .andExpect(jsonPath("$.taxonomy.manual.categories", hasSize<Int>(1)))
                .andExpect(jsonPath("$.taxonomy.manual.categories[0].codeValue", equalTo("A")))
        }
    }

    @Nested
    inner class InvalidCsvs {
        @Test
        fun `binary files are rejected with an appropriate message`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", imageFile.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "The file is not a valid CSV format"
                        )
                    )
                )
        }

        @Test
        fun `returns an error about missing columns when both video ID and category code columns are missing`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidBothColumnsMissing.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error about missing columns when no data rows are given and no category code column is present`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidNoCategoryCodeColumnNoData.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error about missing columns when columns and values are invalid`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidWrongColumnsAndWrongData.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error when file have no Category Code column`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", noCategoryCodeColumn.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "The file must have both 'Category Code' and 'ID' columns"
                        )
                    )
                )
        }

        @Test
        fun `returns an error when video ID value is missing`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidNoVideoIdValue.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "Rows 1 are missing a video ID"
                        )
                    )
                )
        }

        @Test
        fun `returns an error when video ID value is invalid`() {
            addCategory(CategoryFactory.sample(code = "PST"))

            mockMvc.perform(
                multipart("/v1/videos/categories")
                    .file("file", invalidData.file.readBytes())
                    .asBoclipsEmployee()
            ).andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.message", equalTo(
                            "Rows 1 contain invalid Video IDs - one"
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
                        equalTo("Rows 1 contain invalid or unknown category codes - gibberish")
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
                            "Rows 3 contain invalid or unknown category codes - INVALID, " +
                                "Rows 1, 2 contain invalid Video IDs - one, two"
                        )
                    )
                )
        }
    }
}
