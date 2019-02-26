package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI
import java.net.URL
import java.time.ZonedDateTime

class CollectionsControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `create a collection`() {
        val collectionUrl = mockMvc.perform(
            post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "a collection"}""")
                .asTeacher()
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", containsString("/collections/")))
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(get(collectionUrl).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(isEmptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.title", equalTo("a collection")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andReturn()
    }

    @Test
    fun `gets all user collections`() {
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
        addVideo(collectionId, saveVideo(title = "a video title").value)

        mockMvc.perform(get("/v1/collections").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].title", equalTo("a video title")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
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
            .andExpect(jsonPath("$.title", equalTo("My Videos")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andExpect(jsonPath("$._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.removeVideo.templated", equalTo(true)))
    }

    @Test
    fun `collection includes an updatedAt timestamp`() {
        val collectionId = collectionService.create(owner = UserId("teacher@gmail.com"), title = "Collection").id.value

        val moment = ZonedDateTime.now()
        val result = getCollection(collectionId).andReturn()
        val updatedAt = JsonPath.read<String>(result.response.contentAsString, "$.updatedAt")

        assertThat(ZonedDateTime.parse(updatedAt)).isBetween(moment.minusSeconds(10), moment.plusSeconds(10))
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

    @Test
    fun `rename a collection`() {
        val email = "teacher@gmail.com"
        val collectionId = collectionService.create(owner = UserId(email), title = "My Special Collection").id.value

        assertCollectionName(collectionId, "My Special Collection")
        renameCollection(collectionId, "New Name")
        assertCollectionName(collectionId, "New Name")
    }

    @Test
    fun `delete a collection`() {
        val email = "teacher@gmail.com"
        val collectionId = collectionService.create(owner = UserId(email), title = "My Special Collection").id.value

        mockMvc.perform(delete(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).asTeacher())
                .andExpect(status().isNoContent)
        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
                .andExpect(status().isNotFound)
    }

    fun addVideo(collectionId: String, videoId: String) {
        mockMvc.perform(put(addVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    fun removeVideo(collectionId: String, videoId: String) {
        mockMvc.perform(delete(removeVideoLink(collectionId, videoId)).asTeacher())
                .andExpect(status().isNoContent)
    }

    fun renameCollection(collectionId: String, title: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""").asTeacher())
                .andExpect(status().isNoContent)
    }

    fun createCollection(title: String = "a collection name") =
        mockMvc.perform(post("/v1/collections").contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""").asTeacher())
            .andExpect(status().isCreated)
            .andReturn().response.getHeader("Location")!!.substringAfterLast("/")

    fun getCollection(collectionId: String): ResultActions {
        return mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
    }

    fun assertCollectionName(collectionId: String, expectedTitle: String) {
        getCollection(collectionId).andExpect(jsonPath("$.title", equalTo(expectedTitle)))
    }

    fun assertCollectionSize(collectionId: String, expectedSize: Int): ResultActions {
        return getCollection(collectionId)
            .andExpect(jsonPath("$.videos", hasSize<Any>(expectedSize)))
    }

    private fun addVideoLink(collectionId: String, videoId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractVideoLink("addVideo", videoId)
    }

    private fun removeVideoLink(collectionId: String, videoId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractVideoLink("removeVideo", videoId)
    }

    private fun selfLink(collectionId: String): URI {
        return getCollection(collectionId)
                .andReturn()
                .extractLink("self")
    }

    private fun MvcResult.extractLink(relName: String): URI {
        return URI(JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href"))
    }

    private fun MvcResult.extractVideoLink(relName: String, videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href")
        return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
    }
}

