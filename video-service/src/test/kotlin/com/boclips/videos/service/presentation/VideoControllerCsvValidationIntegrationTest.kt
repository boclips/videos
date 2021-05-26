package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.*
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class VideoControllerCsvValidationIntegrationTest: AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Value("classpath:valid.csv")
    lateinit var validCategoryCsv: Resource

    @Value("classpath:invalid_category_code.csv")
    lateinit var invalidCategoryCodeCsv: Resource

    @Value("classpath:invalid_data.csv")
    lateinit var invalidData: Resource

    @Value("classpath:headers_only.csv")
    lateinit var headersOnly: Resource

    @Value("classpath:id_column_only_no_data.csv")
    lateinit var idColumnNoData: Resource

    @Value("classpath:id_column_with_data.csv")
    lateinit var idColumnWithData: Resource

    @Value("classpath:image.csv")
    lateinit var imageFile: Resource

    @Value("classpath:missing_id_value.csv")
    lateinit var missingId: Resource

    @Value("classpath:invalid_columns.csv")
    lateinit var invalidColumns: Resource

    @Test
    fun `can validate a csv of video to category tags`() {
        addCategory(CategoryFactory.sample(code = "A"))

        mockMvc.perform(
            multipart("/v1/videos/categories")
                .file("file", validCategoryCsv.file.readBytes())
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", equalTo("Data has been successfully imported!")))
    }

    @Test
    fun `invalid category tags are rejected`() {
        mockMvc.perform(
            multipart("/v1/videos/categories")
                .file("file", invalidCategoryCodeCsv.file.readBytes())
                .asBoclipsEmployee()
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", equalTo("Rows 1 contain invalid or unknown category codes - gibberish")))
    }

    @Test
    fun `invalid csvs are rejected with the validation failures returned`() {
        addCategory(CategoryFactory.sample(code = "PST"))

        mockMvc.perform(
            multipart("/v1/videos/categories")
                .file("file", invalidCategoryCodeCsv.file.readBytes())
                .asBoclipsEmployee()
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", equalTo(
                "Rows 1 contain invalid or unknown category codes - gibberish")))
    }

    @Test
    fun `image files are rejected with an appropriate message`() {
        addCategory(CategoryFactory.sample(code = "PST"))

        mockMvc.perform(
            multipart("/v1/videos/categories")
                .file("file", imageFile.file.readBytes())
                .asBoclipsEmployee()
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", equalTo(
                "The file is not a valid CSV format")))
    }
}
