package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI

class CollectionsControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `empty default collection`() {
        mockMvc.perform(get("/v1/collections/default").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.owner", `is`("teacher@gmail.com")))
                .andExpect(jsonPath("$.title", `is`("")))
                .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
                .andExpect(jsonPath("$._links.self.href", not(isEmptyString())))
                .andExpect(jsonPath("$._links.addVideo.href", not(isEmptyString())))
                .andExpect(jsonPath("$._links.addVideo.templated", `is`(true)))
    }

    @Test
    fun `add video to default collection`() {
        saveVideo(videoId = 123, title = "a video title")

        val addVideoLink = mockMvc.perform(get("/v1/collections/default").asTeacher())
                .andReturn()
                .extractVideoLink(videoId = "123")

        mockMvc.perform(put(addVideoLink).asTeacher())
                .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/default").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$.videos[0].id", `is`("123")))
                .andExpect(jsonPath("$.videos[0].title", `is`("a video title")))
    }
}

fun MvcResult.extractVideoLink(videoId: String): URI {
    val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.addVideo.href")
    return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
}