package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.infrastructure.collection.CollectionVisibilityDocument
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.ZonedDateTime
import java.util.Date

class CollectionsControllerIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
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
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", not(isEmptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.createdBy", equalTo("Teacher")))
            .andExpect(jsonPath("$.title", equalTo("a collection")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))
            .andExpect(jsonPath("$.ageRange").doesNotExist())
            .andExpect(jsonPath("$.subjects").isEmpty)
            .andReturn()
    }

    @Test
    fun `highlighting collection from Boclips employees`() {
        val email = "terry@boclips.com"

        val collectionUrl = mockMvc.perform(
            post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "a collection"}""")
                .asBoclipsEmployee(email)
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", containsString("/collections/")))
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(get(collectionUrl).asBoclipsEmployee(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.createdBy", equalTo("Boclips")))
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
        updateCollectionToBePublic(collectionId)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher("anotherteacher@boclips.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.mine", equalTo(false)))
    }

    @Test
    fun `get all bookmarked collections paginates`() {
        createCollection("collection 2").apply {
            updateCollectionToBePublic(this)
            bookmarkCollection(this, "notTheOwner@gmail.com")
        }
        createCollection("collection 1").apply {
            updateCollectionToBePublic(this)
            bookmarkCollection(this, "notTheOwner@gmail.com")
        }

        mockMvc.perform(get("/v1/collections?projection=list&page=0&size=1&bookmarked=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.unbookmark.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.bookmark").doesNotExist())

        mockMvc.perform(get("/v1/collections?projection=list&page=1&size=1&bookmarked=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 2")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `bookmark collections returns updated collection`() {
        val collectionId = createCollection("collection 1").apply {
            updateCollectionToBePublic(this)
        }

        mockMvc.perform(patch("/v1/collections/$collectionId?bookmarked=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", not(isEmptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.mine", equalTo(false)))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.unbookmark.href").exists())
            .andExpect(jsonPath("$._links.bookmark").doesNotExist())
    }

    @Test
    fun `unbookmark collections returns updated collection`() {
        val collectionId = createCollection("collection 1").apply {
            updateCollectionToBePublic(this)
            bookmarkCollection(this, "notTheOwner@gmail.com")
        }

        mockMvc.perform(patch("/v1/collections/$collectionId?bookmarked=false").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", not(isEmptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.mine", equalTo(false)))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.bookmark.href").exists())
            .andExpect(jsonPath("$._links.unbookmark").doesNotExist())
    }

    @Test
    fun `collection includes an updatedAt timestamp`() {
        val collectionId = collectionRepository.create(
            owner = UserId("teacher@gmail.com"),
            title = "Collection",
            createdByBoclips = false,
            public = false
        ).id.value

        val moment = ZonedDateTime.now()
        val result = getCollection(collectionId).andReturn()
        val updatedAt = JsonPath.read<String>(result.response.contentAsString, "$.updatedAt")

        assertThat(ZonedDateTime.parse(updatedAt)).isBetween(moment.minusSeconds(10), moment.plusSeconds(10))
    }

    @Test
    fun `fetching a private owned collection`() {
        val collectionId = createCollection("collection from a teacher")

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.remove.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.edit.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.removeVideo.href", not(isEmptyString())))
    }

    @Test
    fun `fetching a non-existent collection returns 404`() {
        mockMvc.perform(get("/v1/collections/${ObjectId().toHexString()}").asTeacher())
            .andExpect(status().isNotFound)
            .andExpect(content().string(isEmptyString()))
    }

    @Test
    fun `fetching a public collection owned by other teacher omits edit links`() {
        val collectionId = createCollection("collection from a teacher")
        updateCollectionToBePublic(collectionId)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher("anotherteacher@boclips.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$._links.self.href", not(isEmptyString())))
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
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$.videos[0].id", equalTo(videoId)))
            .andExpect(jsonPath("$.videos[0].title", nullValue()))
            .andExpect(jsonPath("$.videos[0].description", nullValue()))
            .andExpect(jsonPath("$.videos[0].playback", nullValue()))
            .andExpect(jsonPath("$.videos[0].subjects", hasSize<Any>(0)))
            .andExpect(jsonPath("$.videos[0]._links.self.href", not(isEmptyString())))
            .andReturn()
    }

    @Test
    fun `fetching a specific collection using details projection returns deep details`() {
        val collectionId = createCollection("collection 1")
        val videoId = saveVideo(title = "a video title", contentProvider = "A content provider").value
        addVideo(collectionId, videoId)

        mockMvc.perform(get("/v1/collections/$collectionId?projection=details").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
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
            .andExpect(jsonPath("$.videos[0].playback.thumbnailUrl", not(isEmptyOrNullString())))
            .andExpect(jsonPath("$.videos[0].subjects", not(nullValue())))
            .andExpect(jsonPath("$.videos[0]._links.self.href", not(isEmptyString())))
            .andReturn()
    }

    @Test
    fun `fetching a collection as a viewer`() {
        val collectionId = "5c55697860fef77aa4af323a"
        val userId = "viewer-123@testing.com"
        mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection(MongoCollectionRepository.collectionName)
            .insertOne(
                Document()
                    .append("_id", ObjectId(collectionId))
                    .append("title", "My Videos")
                    .append("owner", "a4efeee2-0166-4371-ba72-0fa5a13c9aca")
                    .append("viewerIds", listOf(userId))
                    .append("updatedAt", Date())
                    .append("videos", emptyList<VideoId>())
            )

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(userId))
            .andExpect(status().isOk)
    }

    @Test
    fun `add and remove video from a specific collection`() {
        val email = "teacher@gmail.com"
        val videoId = saveVideo(title = "a video title").value
        val collectionId =
            collectionRepository.create(
                owner = UserId(email),
                title = "My Special Collection",
                createdByBoclips = false,
                public = false
            )
                .id.value

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

    @Test
    fun `validates collection update request`() {
        val collectionId = createCollectionWithTitle("My Collection for ages")

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"ageRange": {"min": 10000, "max": 0}}""").asTeacher()
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.message",
                    containsString("Age range min must be less than or equal to 19")
                )
            )
    }

    @Test
    fun `adds age range to the existing collection`() {
        val collectionId = createCollectionWithTitle("My Collection for ages")

        getCollection(collectionId)
            .andExpect(jsonPath("$.ageRange", nullValue()))

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"ageRange": {"min": 3, "max": 9}}""").asTeacher()
        )
            .andExpect(status().isNoContent)

        getCollection(collectionId)
            .andExpect(jsonPath("$.ageRange.min", equalTo(3)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(9)))
    }

    @Test
    fun `does not update age range for existing collection if age range in request has null values`() {
        val collectionId = createCollectionWithTitle("My Collection for more ages")

        getCollection(collectionId)
            .andExpect(jsonPath("$.ageRange", nullValue()))

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"ageRange": {"min": 3, "max": 9}}""").asTeacher()
        )

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"ageRange": {"min": null, "max": null}}""").asTeacher()
        )

        getCollection(collectionId)
            .andExpect(jsonPath("$.ageRange.min", equalTo(3)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(9)))
    }

    @Test
    fun `adds two subjects to the existing collection, then rename the collection, subjects persist`() {
        val collectionId = createCollectionWithTitle("My Collection for Subjects")

        getCollection(collectionId)
            .andExpect(jsonPath("$.subjects", hasSize<Any>(0)))

        val frenchSubject = saveSubject("French")
        val germanSubject = saveSubject("German")

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${frenchSubject.value}", "${germanSubject.value}"]}""").asTeacher()
        )
            .andExpect(status().isNoContent)

        updateCollectionToBePublicAndRename(collectionId, "My new shiny title")

        getCollection(collectionId).andExpect(jsonPath("$.subjects", hasSize<Any>(2)))
    }

    @Test
    fun `will return an attachment in details projection`() {
        val collectionId = "5c55697860fef77aa4af323b"

        createCollectionWithAttachment(collectionId, "description", "LESSON_PLAN", "https://example.com/download")

        mockMvc.perform(get("/v1/collections/$collectionId?projection=details").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.attachments", hasSize<Any>(1)))
            .andExpect(jsonPath("$.attachments[0].id", notNullValue()))
            .andExpect(jsonPath("$.attachments[0].description", equalTo("description")))
            .andExpect(jsonPath("$.attachments[0].type", equalTo("LESSON_PLAN")))
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("https://example.com/download")))
    }

    @Test
    fun `will return an attachment in list projection`() {
        val collectionId = "5c55697860fef77aa4af323b"

        createCollectionWithAttachment(collectionId, "description", "LESSON_PLAN", "https://example.com/download")

        mockMvc.perform(get("/v1/collections/$collectionId?projection=list").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.attachments", hasSize<Any>(1)))
            .andExpect(jsonPath("$.attachments[0].id", notNullValue()))
            .andExpect(jsonPath("$.attachments[0].description", equalTo("description")))
            .andExpect(jsonPath("$.attachments[0].type", equalTo("LESSON_PLAN")))
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("https://example.com/download")))
    }

    private fun createCollectionWithAttachment(
        collectionId: String,
        description: String,
        type: String,
        linkToResource: String
    ) {
        mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection(MongoCollectionRepository.collectionName)
            .insertOne(
                Document()
                    .append("_id", ObjectId(collectionId))
                    .append("title", "Collection With Attachment")
                    .append("owner", "a4efeee2-0166-4371-ba72-0fa5a13c9aca")
                    .append("visibility", CollectionVisibilityDocument.PUBLIC)
                    .append("updatedAt", Date())
                    .append("videos", emptyList<VideoId>())
                    .append(
                        "attachments", setOf(
                            AttachmentDocument(
                                id = ObjectId(),
                                description = description,
                                type = type,
                                linkToResource = linkToResource
                            )
                        )
                    )
            )
    }

    private fun removeVideo(collectionId: String, videoId: String) {
        mockMvc.perform(delete(removeVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun renameCollection(collectionId: String, title: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""").asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun bookmarkCollection(collectionId: String, user: String) {
        mockMvc.perform(patch(bookmarkLink(collectionId, user)).contentType(MediaType.APPLICATION_JSON).asTeacher(user))
            .andExpect(status().isOk)
    }

    private fun updateCollectionToBePublicAndRename(collectionId: String, title: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"public": "true", "title": "$title"}""").asTeacher())
            .andExpect(status().isNoContent)
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

    private fun removeVideoLink(collectionId: String, videoId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractVideoLink("removeVideo", videoId)
    }

    private fun bookmarkLink(collectionId: String, user: String): URI {
        return getCollection(collectionId, user)
            .andReturn()
            .extractLink("bookmark")
    }
}

