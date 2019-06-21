package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.UserId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asSubjectClassifier
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
import org.hamcrest.Matchers.nullValue
import org.hamcrest.collection.IsIn
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
    lateinit var collectionRepository: CollectionRepository

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
    fun `gets all user collections with full details`() {
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
        addVideo(collectionId, saveVideo(title = "a video title").value)

        mockMvc.perform(get("/v1/collections?projection=details&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].title", equalTo("a video title")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", endsWith(collectionId)))
            .andExpect(jsonPath("$._embedded.collections[0]._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.details.href").exists())
            .andExpect(jsonPath("$._links.list.href").exists())
            .andReturn()
    }

    @Test
    fun `gets all user collections with basic video details`() {
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
        val savedVideoId = saveVideo(title = "a video title")
        addVideo(collectionId, savedVideoId.value)

        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(true)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].id", equalTo(savedVideoId.value)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", endsWith(collectionId)))
            .andExpect(jsonPath("$._embedded.collections[0]._links.addVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.removeVideo.href", not(isEmptyString())))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.details.href").exists())
            .andExpect(jsonPath("$._links.list.href").exists())
            .andReturn()
    }

    @Test
    fun `get another users private collections`() {
        val savedVideoId = saveVideo()
        val collectionId = createCollection()
        addVideo(collectionId, savedVideoId.value)

        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asSubjectClassifier())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
    }

    @Test
    fun `getting user collections when empty`() {
        mockMvc.perform(get("/v1/collections?projection=list&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(0)))
    }

    @Test
    fun `cannot fetch collection when owner does not match user`() {
        createCollection("collection 1")

        mockMvc.perform(get("/v1/collections?projection=details&owner=teacher@gmail.com").asTeacher("notTheOwner@gmail.com"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `filter all public collections and use pagination`() {
        updateCollectionToBePublic(createCollection("collection 1"))
        updateCollectionToBePublic(createCollection("collection 2"))

        mockMvc.perform(get("/v1/collections?projection=list&page=0&size=1&public=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.bookmark.href").exists())
            .andExpect(jsonPath("$._embedded.collections[0]._links.unbookmark").doesNotExist())

        mockMvc.perform(get("/v1/collections?projection=list&page=1&size=1&public=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 2")))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `query search public collections`() {
        updateCollectionToBePublic(createCollection("five ponies were eating grass"))
        updateCollectionToBePublic(createCollection("while a car and a truck crashed"))

        mockMvc.perform(get("/v1/collections?query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
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
            createdByBoclips = false
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
    fun `add and remove video from a specific collection`() {
        val email = "teacher@gmail.com"
        val videoId = saveVideo(title = "a video title").value
        val collectionId =
            collectionRepository.create(
                owner = UserId(email),
                title = "My Special Collection",
                createdByBoclips = false
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
    fun `adds two subjects to the existing collection`() {
        val collectionId = createCollectionWithTitle("My Collection for Subjects")

        getCollection(collectionId)
            .andExpect(jsonPath("$.subjects", hasSize<Any>(0)))

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["SubjectOneId", "SubjectTwoId"]}""").asTeacher()
        )
            .andExpect(status().isNoContent)

        getCollection(collectionId)
            .andExpect(jsonPath("$.subjects", hasSize<Any>(2)))
            .andExpect(jsonPath("$.subjects[0].id", IsIn(listOf("SubjectOneId", "SubjectTwoId"))))
            .andExpect(jsonPath("$.subjects[1].id", IsIn(listOf("SubjectOneId", "SubjectTwoId"))))
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
    fun `adds two subjects to the existing collection, then rename the collection`() {
        val collectionId = createCollectionWithTitle("My Collection for Subjects")

        getCollection(collectionId)
            .andExpect(jsonPath("$.subjects", hasSize<Any>(0)))

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["SubjectOneId", "SubjectTwoId"]}""").asTeacher()
        )
            .andExpect(status().isNoContent)

        updateCollectionToBePublicAndRename(collectionId, "My new shiny title")

        getCollection(collectionId)
            .andExpect(jsonPath("$.subjects", hasSize<Any>(2)))
    }

    @Test
    fun `can filter public collections by subjects`() {
        val collectionWithSubjectsId = createCollectionWithTitle("My Collection for with Subjects")
        val collectionWithoutSubjectsId = createCollectionWithTitle("My Collection for without Subjects")

        mockMvc.perform(
            patch(selfLink(collectionWithSubjectsId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["SubjectOneId"]}""").asTeacher()
        )

        updateCollectionToBePublic(collectionWithSubjectsId)
        updateCollectionToBePublic(collectionWithoutSubjectsId)

        mockMvc.perform(get("/v1/collections?subject=SubjectOneId&projection=details&public=true").asTeacher("teacher@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
    }

    private fun createCollectionWithTitle(title: String): String {
        val email = "teacher@gmail.com"
        return collectionRepository.create(owner = UserId(email), title = title, createdByBoclips = false).id.value
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

    private fun bookmarkCollection(collectionId: String, user: String) {
        mockMvc.perform(patch(bookmarkLink(collectionId, user)).contentType(MediaType.APPLICATION_JSON).asTeacher(user))
            .andExpect(status().isOk)
    }

    private fun updateCollectionToBePublicAndRename(collectionId: String, title: String) {
        mockMvc.perform(patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"public": "true", "title": "$title"}""").asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun createCollection(title: String = "a collection name") =
        mockMvc.perform(post("/v1/collections").contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""").asTeacher())
            .andExpect(status().isCreated)
            .andReturn().response.getHeader("Location")!!.substringAfterLast("/")

    private fun getCollection(collectionId: String, user: String = "teacher@gmail.com"): ResultActions {
        return mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(user))
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

    private fun bookmarkLink(collectionId: String, user: String): URI {
        return getCollection(collectionId, user)
            .andReturn()
            .extractLink("bookmark")
    }

    private fun MvcResult.extractLink(relName: String): URI {
        return URI(JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href"))
    }

    private fun MvcResult.extractVideoLink(relName: String, videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href")
        return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
    }
}

