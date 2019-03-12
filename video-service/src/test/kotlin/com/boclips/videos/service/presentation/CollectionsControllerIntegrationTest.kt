package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
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
    fun `gets all user collections with full details`() {
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
        addVideo(collectionId, saveVideo(title = "a video title").value)

        mockMvc.perform(get("/v1/collections?projection=details").asTeacher())
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
            .andExpect(jsonPath("$._links.self.href", endsWith("/v1/collections?projection=details")))
            .andExpect(jsonPath("$._links.details.href", endsWith("/v1/collections?projection=details")))
            .andExpect(jsonPath("$._links.list.href", endsWith("/v1/collections?projection=list")))
            .andReturn()
    }

    @Test
    fun `gets all user collections with basic video details`() {
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
        val savedVideoAssetId = saveVideo(title = "a video title")
        addVideo(collectionId, savedVideoAssetId.value)

        mockMvc.perform(get("/v1/collections?projection=list").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].id", equalTo(savedVideoAssetId.value)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.self.href", endsWith("/v1/collections?projection=list")))
            .andExpect(jsonPath("$._links.details.href", endsWith("/v1/collections?projection=details")))
            .andExpect(jsonPath("$._links.list.href", endsWith("/v1/collections?projection=list")))
            .andReturn()
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
        val collectionId = createCollectionWithTitle("My Special Collection")

        assertCollectionName(collectionId, "My Special Collection")
        renameCollection(collectionId, "New Name")
        assertCollectionName(collectionId, "New Name")
    }

    @Test
    fun `mark a collection as public and private`() {
        val collectionId = createCollectionWithTitle("My Special Collection")

        assertCollectionIsPrivate(collectionId)
        updateCollectionToBePublic(collectionId)
        assertCollectionIsPublic(collectionId)
    }

    @Test
    fun `change more than one property of a collection`() {
        val collectionId = createCollectionWithTitle("My Special Collection")

        assertCollectionIsPrivate(collectionId)
        assertCollectionName(collectionId, "My Special Collection")
        updateCollectionToBePublicAndRename(collectionId, "New Name")
        assertCollectionName(collectionId, "New Name")
        assertCollectionIsPublic(collectionId)
    }

    @Test
    fun `delete a collection`() {
        val collectionId = createCollectionWithTitle("My Special Collection")

        mockMvc.perform(delete(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).asTeacher())
            .andExpect(status().isNoContent)
        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isNotFound)
    }

    private fun createCollectionWithTitle(title: String): String {
        val email = "teacher@gmail.com"
        val collectionId = collectionService.create(owner = UserId(email), title = title).id.value
        return collectionId
    }

    private fun addVideo(collectionId: String, videoId: String) {
        mockMvc.perform(put(addVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun removeVideo(collectionId: String, videoId: String) {
        mockMvc.perform(delete(removeVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun renameCollection(collectionId: String, title: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""").asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun updateCollectionToBePublic(collectionId: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"public": "true"}""").asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun updateCollectionToBePublicAndRename(collectionId: String, title: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"public": "true", "title": "$title"}""").asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun createCollection(title: String = "a collection name") =
        mockMvc.perform(post("/v1/collections").contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""").asTeacher())
            .andExpect(status().isCreated)
            .andReturn().response.getHeader("Location")!!.substringAfterLast("/")

    private fun getCollection(collectionId: String): ResultActions {
        return mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
    }

    private fun assertCollectionIsPublic(collectionId: String) {
        getCollection(collectionId).andExpect(jsonPath("$.public", equalTo(true)))
    }

    private fun assertCollectionIsPrivate(collectionId: String) {
        getCollection(collectionId).andExpect(jsonPath("$.public", equalTo(false)))
    }

    private fun assertCollectionName(collectionId: String, expectedTitle: String) {
        getCollection(collectionId).andExpect(jsonPath("$.title", equalTo(expectedTitle)))
    }

    private fun assertCollectionSize(collectionId: String, expectedSize: Int): ResultActions {
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

