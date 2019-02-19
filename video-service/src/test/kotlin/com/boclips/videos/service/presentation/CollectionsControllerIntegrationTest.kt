package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.bson.types.ObjectId
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
import org.springframework.test.web.servlet.ResultActions
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
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
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
        getCollection("default")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(isEmptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.title",  equalTo("My Videos")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andExpect(jsonPath("$._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.removeVideo.templated", equalTo(true)))
    }

    @Test
    fun `fetching a non-existent collection returns 404`() {
        mockMvc.perform(get("/v1/collections/${ObjectId().toHexString()}").asTeacher())
            .andExpect(status().isNotFound)
            .andExpect(content().string(isEmptyString()))
    }

    @Test
    fun `add video to default collection and retrieve it`() {
        val videoId = saveVideo(title = "a video title")
        val collectionId = "default"

        assertCollectionSize(collectionId, 0)
        addVideo(collectionId, videoId.value)

        getCollection(collectionId)
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$.videos[0].id", equalTo(videoId.value)))
            .andExpect(jsonPath("$.videos[0].title", equalTo("a video title")))
    }

    @Test
    fun `remove video from the default collection`() {
        val videoId = saveVideo(title = "a video title").value
        val collectionId = "default"

        assertCollectionSize(collectionId, 0)
        addVideo(collectionId, videoId)
        assertCollectionSize(collectionId, 1)
        removeVideo(collectionId, videoId)
        assertCollectionSize(collectionId, 0)
    }

    @Test
    fun `add and remove video from a specific collection`() {
        val email = "teacher@gmail.com"
        val videoId = saveVideo(title = "a video title").value
        val collectionId = collectionService.create(owner = UserId(email), title = "My Special Collection").id.value

        assertCollectionSize(collectionId, 0)
        addVideo(collectionId, videoId)
        assertCollectionSize(collectionId, 1)
        removeVideo(collectionId, videoId)
        assertCollectionSize(collectionId, 0)
    }

    fun addVideo(collectionId: String, videoId: String) {
        mockMvc.perform(put(addVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    fun removeVideo(collectionId: String, videoId: String) {
        mockMvc.perform(delete(removeVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    fun getCollection(collectionId: String): ResultActions {
        return mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
    }

    fun assertCollectionSize(collectionId: String, expectedSize: Int): ResultActions {
        return getCollection(collectionId)
            .andExpect(jsonPath("$.videos", hasSize<Any>(expectedSize)))
    }

    private fun addVideoLink(collectionId: String, videoId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractLink("addVideo", videoId)
    }

    private fun removeVideoLink(collectionId: String, videoId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractLink("removeVideo", videoId)
    }

    private fun MvcResult.extractLink(relName: String, videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href")
        return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
    }
}

