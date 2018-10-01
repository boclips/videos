package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.authenticateAsTeacher
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Before
    fun setUp() {
        saveVideo(videoId = 123,
                referenceId = "ref-id-1",
                title = "powerful video about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = "00:01:00",
                contentProvider = "cp")
    }

    @Test
    fun `search for videos`() {
        mockMvc.perform(get("/v1/videos/search?query=powerful").authenticateAsTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.query", equalTo("powerful")))
                .andExpect(jsonPath("$.videos[0].id", equalTo("123")))
                .andExpect(jsonPath("$.videos[0].title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.videos[0].description", equalTo("test description 3")))
                .andExpect(jsonPath("$.videos[0].releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.videos[0].duration", equalTo("PT1M")))
                .andExpect(jsonPath("$.videos[0].contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$.videos[0].streamUrl", equalTo("https://stream/mpegdash/video-1.mp4")))
                .andExpect(jsonPath("$.videos[0].thumbnailUrl", equalTo("https://thumbnail/thumbnail-1.mp4")))
                .andExpect(jsonPath("$.videos[0]._links.self.href", containsString("/videos/123")))
    }

    @Test
    fun `returns 200 for OPTIONS requests`() {
        mockMvc.perform(options("/v1/videos/search"))
                .andExpect(status().isOk)
    }

    @Test
    fun `returns 401 for anonymous search request`() {
        mockMvc.perform(get("/v1/videos/search"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos/search").authenticateAsTeacher())
                .andExpect(status().`is`(400))
    }

    @Test
    fun `video details`() {
        mockMvc.perform(get("/v1/videos/123").authenticateAsTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo("123")))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.duration", equalTo("PT1M")))
                .andExpect(jsonPath("$.contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$.streamUrl", equalTo("https://stream/mpegdash/video-1.mp4")))
                .andExpect(jsonPath("$.thumbnailUrl", equalTo("https://thumbnail/thumbnail-1.mp4")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/123")))
    }

    @Test
    fun `returns 401 for anonymous video request`() {
        mockMvc.perform(get("/v1/videos/123"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 404 for inexistent video`() {
        mockMvc.perform(get("/v1/videos/9999").authenticateAsTeacher())
                .andExpect(status().`is`(404))
    }

    @Ignore
    // TODO to discuss with Jacek
    inner class RequestIdIntegrationTest {
        @Test
        fun `searchId is unique`() {
            val searchId1 = extractSearchId()
            val searchId2 = extractSearchId()

            assertThat(searchId1).isNotBlank()
            assertThat(searchId1).isNotEqualTo(searchId2)
        }

        @Test
        fun `contains a request id`() {
            mockMvc.perform(get("/v1/videos/search?query=powerful").authenticateAsTeacher())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.searchId", not(isEmptyOrNullString())))
        }


        private fun extractSearchId(): String {
            val content = mockMvc.perform(get("/v1/videos/search?query=powerful").authenticateAsTeacher())
                    .andExpect(status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            return ObjectMapper().readValue(content, Map::class.java)["searchId"].toString()
        }
    }

}

