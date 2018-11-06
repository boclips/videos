package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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
    fun `returns 200 videos with text query`() {
        mockMvc.perform(get("/v1/videos?query=powerful").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo("123")))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT1M")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.streamUrl", equalTo("https://stream/mpegdash/video-1.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-1.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/123")))
    }

    @Test
    fun `returns empty videos array when nothing matches`() {
        mockMvc.perform(get("/v1/videos?query=whatdohorseseat").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(0)))
    }

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos").asTeacher())
                .andExpect(status().`is`(400))
    }

    @Test
    fun `returns 200 for valid video`() {
        mockMvc.perform(get("/v1/videos/123").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo("123")))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT1M")))
                .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/mpegdash/video-1.mp4")))
                .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-1.mp4")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/123")))
    }

    @Test
    fun `returns 404 for inexistent video`() {
        mockMvc.perform(get("/v1/videos/9999").asTeacher())
                .andExpect(status().`is`(404))
    }

    @Test
    fun `returns 200 when video is deleted`() {
        mockMvc.perform(delete("/v1/videos/123").asOperator())
                .andExpect(status().`is`(200))
    }

    @Test
    fun `returns correlation id`() {
        mockMvc.perform(get("/v1/videos?query=powerful").header("X-Correlation-ID", "correlation-id").asTeacher())
                .andExpect(status().isOk)
                .andExpect(header().string("X-Correlation-ID", "correlation-id"))
    }

    @Test
    fun `records search events`() {
        mockMvc.perform(get("/v1/videos?query=bugs").header("X-Correlation-ID", "correlation-id").asTeacher())
                .andExpect(status().isOk)

        val searchEvent = eventService.latestInteractions().last()
        assertThat(searchEvent.description).startsWith("Search for 'bugs'")
    }
}

