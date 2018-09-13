package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.withTeacher
import org.hamcrest.Matchers.*
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `search for videos`() {
        mockMvc.perform(get("/v1/videos?query=powerful").withTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo("test-id-3")))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].duration", equalTo("PT1M")))
                .andExpect(jsonPath("$._embedded.videos[0].contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$._embedded.videos[0].streamUrl", equalTo("https://stream/mpegdash/video-3.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0].thumbnailUrl", equalTo("https://thumbnail/thumbnail-3.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/test-id-3")))
                .andExpect(jsonPath("$._links.search.href", containsString("/videos?query=")))
    }

    @Test
    fun `returns 401 for anonymous search request`() {
        mockMvc.perform(get("/v1/videos"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos").withTeacher())
                .andExpect(status().`is`(400))
    }

    @Test
    fun `returns empty videos array when there are no results`() {
        mockMvc.perform(get("/v1/videos?query=somethingthatdoesntexistever").withTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", emptyIterable<Any>()))
    }

    @Test
    fun `video details`() {
        mockMvc.perform(get("/v1/videos/test-id-3").withTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo("test-id-3")))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.duration", equalTo("PT1M")))
                .andExpect(jsonPath("$.contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$.streamUrl", equalTo("https://stream/mpegdash/video-3.mp4")))
                .andExpect(jsonPath("$.thumbnailUrl", equalTo("https://thumbnail/thumbnail-3.mp4")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/test-id-3")))
    }

    @Test
    fun `returns 401 for anonymous video request`() {
        mockMvc.perform(get("/v1/videos/test-id-3"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 404 for inexistent video`() {
        mockMvc.perform(get("/v1/videos/does-not-exist").withTeacher())
                .andExpect(status().`is`(404))
    }
}