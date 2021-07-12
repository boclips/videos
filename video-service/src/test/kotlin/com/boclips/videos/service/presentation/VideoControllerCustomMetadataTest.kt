package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.File

internal class VideoControllerCustomMetadataTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `return no validation error when file is a valid CSV`() {

        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)
        val saveVideo1Id = saveVideo().value

        val header = listOf("ID", "Topic ID", "Topic")
        val row1 = listOf(saveVideo1Id, "", "")

        csvWriter().open(csvName) {
            writeRow(header)
            writeRow(row1)
        }

        val fixture = csvFile.inputStream()

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/v1/custom-metadata")
                .file("file", fixture.readBytes())
                .asBoclipsEmployee()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.message",
                    Matchers.equalTo("Data has been successfully imported!")
                )
            )

        csvFile.delete()
    }

    @Test
    fun `return meaningful error when video id is invalid`() {

        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)
        val saveVideo1Id = "invalid-video-id"

        val header = listOf("ID", "Topic ID", "Topic")
        val row1 = listOf(saveVideo1Id, "", "")

        csvWriter().open(csvName) {
            writeRow(header)
            writeRow(row1)
        }

        val fixture = csvFile.inputStream()

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/v1/custom-metadata")
                .file("file", fixture.readBytes())
                .asBoclipsEmployee()
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.message",
                    Matchers.equalTo("Rows 2 contains invalid video ID")
                )
            )

        csvFile.delete()
    }
}
