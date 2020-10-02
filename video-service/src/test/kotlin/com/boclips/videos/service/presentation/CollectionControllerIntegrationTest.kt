package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.testsupport.*
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZonedDateTime

class CollectionControllerIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `returns a not found response when trying to create with a subject that does not exist`() {
        val phantomSubjectId = ObjectId().toHexString()

        mockMvc.perform(
            post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "a collection",
                        "subjects": ["$phantomSubjectId"]
                    }
                    """.trimIndent()
                )
                .asTeacher()
        )
            .andExpect(status().isNotFound)
            .andExpect(
                jsonPath(
                    "$.message",
                    containsString(phantomSubjectId)
                )
            )
    }

    @Test
    fun `collection is mine if I created it`() {
        val collectionId = createCollection("collection from a teacher")

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.mine", equalTo(true)))
    }

    @Test
    fun `collection is not mine if I did not create it`() {
        val collectionId = createCollection("collection from a teacher")

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher("anotherteacher@boclips.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.mine", equalTo(false)))
    }

    @Test
    fun `collection includes an updatedAt timestamp`() {
        val collectionId = collectionRepository.create(
            CreateCollectionCommand(
                owner = UserId("teacher@gmail.com"),
                title = "Collection",
                createdByBoclips = false,
                discoverable = false
            )
        ).id.value

        val moment = ZonedDateTime.now()
        val result = getCollection(collectionId).andReturn()
        val updatedAt = JsonPath.read<String>(result.response.contentAsString, "$.updatedAt")

        assertThat(ZonedDateTime.parse(updatedAt)).isBetween(moment.minusSeconds(10), moment.plusSeconds(10))
    }

    @Test
    fun `fetching a owners collection`() {
        val collectionId = createCollection("collection from a teacher")

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$._links.self.href", not(emptyString())))
            .andExpect(jsonPath("$._links.remove.href", not(emptyString())))
            .andExpect(jsonPath("$._links.edit.href", not(emptyString())))
            .andExpect(jsonPath("$._links.addVideo.href", not(emptyString())))
            .andExpect(jsonPath("$._links.removeVideo.href", not(emptyString())))
    }

    @Test
    fun `fetching a non-existent collection returns 404`() {
        mockMvc.perform(get("/v1/collections/${ObjectId().toHexString()}").asTeacher())
            .andExpect(status().isNotFound)
            .andExpect(content().string(emptyString()))
    }

    @Test
    fun `fetching a discoverable collection owned by other teacher omits edit links`() {
        val collectionId = createCollection("collection from a teacher")
        updateCollectionToBeDiscoverable(collectionId)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher("anotherteacher@boclips.com"))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$._links.self.href", not(emptyString())))
            .andExpect(jsonPath("$._links.remove").doesNotExist())
            .andExpect(jsonPath("$._links.edit").doesNotExist())
            .andExpect(jsonPath("$._links.addVideo").doesNotExist())
            .andExpect(jsonPath("$._links.removeVideo").doesNotExist())
    }

    @Test
    fun `fetching a specific collection returns shallow details by default`() {
        val collectionId = createCollection("collection 1")
        val videoId = saveVideo(title = "a video title").value
        addVideo(collectionId, videoId)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$.videos[0].id", equalTo(videoId)))
            .andExpect(jsonPath("$.videos[0].title", nullValue()))
            .andExpect(jsonPath("$.videos[0].description", nullValue()))
            .andExpect(jsonPath("$.videos[0].playback", nullValue()))
            .andExpect(jsonPath("$.videos[0].subjects", hasSize<Any>(0)))
            .andExpect(jsonPath("$.videos[0]._links.self.href", not(emptyString())))
            .andReturn()
    }

    @Test
    fun `fetching a specific collection with youtube video`() {
        val collectionId = createCollection("collection 1")
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "123" ), title = "a video title").value
        addVideo(collectionId, videoId)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "application/hal+json"))
                .andExpect(jsonPath("$.id", equalTo(collectionId)))
                .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
                .andExpect(jsonPath("$.title", equalTo("collection 1")))
                .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$.videos[0].id", equalTo(videoId)))
                .andExpect(jsonPath("$.videos[0].title", nullValue()))
                .andExpect(jsonPath("$.videos[0].description", nullValue()))
                .andExpect(jsonPath("$.videos[0].playback", nullValue()))
                .andExpect(jsonPath("$.videos[0].playback.downloadUrl").doesNotExist())
                .andExpect(jsonPath("$.videos[0].subjects", hasSize<Any>(0)))
                .andExpect(jsonPath("$.videos[0]._links.self.href", not(emptyString())))
                .andReturn()
    }

    @Test
    fun `fetching a specific collection using details projection returns deep details`() {
        val collectionId = createCollection("collection 1")
        val videoId = saveVideo(title = "a video title", contentProvider = "A content provider").value
        addVideo(collectionId, videoId)

        mockMvc.perform(get("/v1/collections/$collectionId?projection=details").asTeacher())
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$.videos[0].id", equalTo(videoId)))
            .andExpect(jsonPath("$.videos[0].title", equalTo("a video title")))
            .andExpect(jsonPath("$.videos[0].description", equalTo("Some description!")))
            .andExpect(jsonPath("$.videos[0].createdBy", equalTo("A content provider")))
            .andExpect(jsonPath("$.videos[0].contentProvider").doesNotExist())
            .andExpect(jsonPath("$.videos[0].playback", not(nullValue())))
            .andExpect(jsonPath("$.videos[0].playback.duration", not(nullValue())))
            .andExpect(jsonPath("$.videos[0].subjects", not(nullValue())))
            .andExpect(jsonPath("$.videos[0]._links.self.href", not(emptyString())))
            .andExpect(jsonPath("$.videos[0].playback._links.thumbnail.href", not(emptyOrNullString())))
            .andReturn()
    }

    @Test
    fun `fetching a collection as a user with a SelectedContent contract for it`() {
        val collectionId = createCollection(title = "Some undiscoverable Collection", discoverable = false)
        createIncludedCollectionsAccessRules("api-user@gmail.com", collectionId)

        mockMvc.perform(get("/v1/collections/$collectionId").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
    }

    @Test
    fun `delete a collection`() {
        val collectionId = createCollectionWithTitle("My Special Collection")

        mockMvc.perform(delete(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).asTeacher())
            .andExpect(status().isNoContent)
        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `cannot delete a collection of another user`() {
        val collectionId =
            createCollectionWithTitle(title = "My Special Collection", email = "teacher@gmail.com", discoverable = true)

        mockMvc.perform(
            delete(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .asTeacher("anotherteacher@gmail.com")
        )
            .andExpect(status().isNotFound)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher("anotherteacher@gmail.com"))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `will return an attachment in details projection`() {
        val collectionId = saveCollection(owner = "teacher@gmail.com")

        updateCollectionAttachment(
            collectionId = collectionId.value,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "description",
            attachmentURL = "https://example.com/download"
        )

        mockMvc.perform(get("/v1/collections/${collectionId.value}?projection=details").asTeacher())
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$.id", equalTo(collectionId.value)))
            .andExpect(jsonPath("$.attachments", hasSize<Any>(1)))
            .andExpect(jsonPath("$.attachments[0].id", notNullValue()))
            .andExpect(jsonPath("$.attachments[0].description", equalTo("description")))
            .andExpect(jsonPath("$.attachments[0].type", equalTo("LESSON_PLAN")))
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("https://example.com/download")))
    }

    @Test
    fun `will return an attachment in list projection`() {
        val collectionId = saveCollection(owner = "teacher@gmail.com")

        updateCollectionAttachment(
            collectionId = collectionId.value,
            attachmentType = "LESSON_PLAN",
            attachmentDescription = "description",
            attachmentURL = "https://example.com/download"
        )

        mockMvc.perform(get("/v1/collections/${collectionId.value}?projection=list").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$.id", equalTo(collectionId.value)))
            .andExpect(jsonPath("$.attachments", hasSize<Any>(1)))
            .andExpect(jsonPath("$.attachments[0].id", notNullValue()))
            .andExpect(jsonPath("$.attachments[0].description", equalTo("description")))
            .andExpect(jsonPath("$.attachments[0].type", equalTo("LESSON_PLAN")))
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("https://example.com/download")))
    }

    @Test
    fun `will return a superuser's bookmarked collections`() {
        createCollection("collection 1", discoverable = true).apply {
            bookmarkCollection(this, "me@gmail.com")
        }
        createCollection("collection 2", discoverable = true).apply {
            bookmarkCollection(this, "notMe@gmail.com")
        }

        mockMvc.perform(
            get("/v1/collections?projection=list&bookmarked=true&page=0&size=30")
                .asBoclipsEmployee(email = "me@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
    }
}
