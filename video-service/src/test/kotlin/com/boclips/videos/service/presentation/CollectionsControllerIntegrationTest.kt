package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.bson.types.ObjectId
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI

class CollectionsControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `gets all user collections`() {
        mockMvc.perform(get("/v1/collections").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", `is`("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("My Videos")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(0)))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.self.href", endsWith("/v1/collections")))
            .andReturn()
    }

    @Test
    fun `empty default collection`() {
        mockMvc.perform(get("/v1/collections/default").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(isEmptyString())))
            .andExpect(jsonPath("$.owner", `is`("teacher@gmail.com")))
            .andExpect(jsonPath("$.title",  equalTo("My Videos")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andExpect(jsonPath("$._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.templated", `is`(true)))
            .andExpect(jsonPath("$._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.removeVideo.templated", `is`(true)))
    }

    @Test
    fun `fetching a non-existent collection returns 404`() {
        mockMvc.perform(get("/v1/collections/${ObjectId().toHexString()}").asTeacher())
            .andExpect(status().`is`(404))
            .andExpect(content().string(isEmptyString()))
    }

    @Test
    fun `add video to default collection and retrieve it`() {
        val videoId = saveVideo(title = "a video title")

        val addVideoLink = mockMvc.perform(get("/v1/collections/default").asTeacher())
            .andReturn()
            .extractVideoAddLink(videoId = videoId.value)

        mockMvc.perform(put(addVideoLink).asTeacher())
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/default").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$.videos[0].id", `is`(videoId.value)))
            .andExpect(jsonPath("$.videos[0].title", `is`("a video title")))
    }

    @Test
    fun `add video to specific collection and retrieve it`() {
        val email = "teacher@gmail.com"
        val videoId = saveVideo(title = "a video title")
        val collectionId = collectionService.create(owner = UserId(email), title = "My Special Collection").id.value

        val response = mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andReturn()

        val addVideoLink = response.extractVideoAddLink(videoId = videoId.value)

        mockMvc.perform(put(addVideoLink).asTeacher(email))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$.videos[0].id", `is`(videoId.value)))
            .andExpect(jsonPath("$.videos[0].title", `is`("a video title")))
    }

    @Test
    fun `remove video from the default collection`() {
        val videoId = saveVideo(title = "a video title")

        val result = mockMvc.perform(get("/v1/collections/default").asTeacher())
            .andReturn()

        val addVideoLink = result.extractVideoAddLink(videoId = videoId.value)
        val removeVideoLink = result.extractVideoRemoveLink(videoId = videoId.value)

        mockMvc.perform(put(addVideoLink).asTeacher())
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/default").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))

        mockMvc.perform(delete(removeVideoLink).asTeacher())
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/default").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
    }

    @Test
    fun `remove video from a specific collection`() {
        val email = "teacher@gmail.com"
        val videoId = saveVideo(title = "a video title")
        val collectionId = collectionService.create(owner = UserId(email), title = "My Special Collection").id.value

        val result = mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andReturn()

        val addVideoLink = result.extractVideoAddLink(videoId = videoId.value)
        val removeVideoLink = result.extractVideoRemoveLink(videoId = videoId.value)

        mockMvc.perform(put(addVideoLink).asTeacher(email))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))

        mockMvc.perform(delete(removeVideoLink).asTeacher(email))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
    }

    private fun MvcResult.extractVideoAddLink(videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.addVideo.href")
        return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
    }

    private fun MvcResult.extractVideoRemoveLink(videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.removeVideo.href")
        return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
    }
}

