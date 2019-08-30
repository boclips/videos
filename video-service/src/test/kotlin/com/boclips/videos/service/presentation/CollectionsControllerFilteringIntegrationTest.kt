package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asSubjectClassifier
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CollectionsControllerFilteringIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `gets all user collections with full details`() {
        val collectionId = createCollection("collection 1")
        createCollection("collection 2")
        addVideo(collectionId, saveVideo(title = "a video title", contentProvider = "A content provider").value)

        mockMvc.perform(get("/v1/collections?projection=details&owner=teacher@gmail.com").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("collection 1")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].title", equalTo("a video title")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].createdBy", equalTo("A content provider")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].contentPartner").doesNotExist())
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
    fun `get collections by viewer with deep video information`() {
        val viewer = "viewer@test.com"

        val savedVideoId = saveVideo()
        val collection = createCollectionForViewer(viewer)
        addVideo(collection.id.value, savedVideoId.value)

        mockMvc.perform(get("/v1/collections/dont-do-this-at-home").asApiUser(viewer))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].mine", equalTo(false)))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].playback.thumbnailUrl", not(isEmptyString())))
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
        createCollection(title = "the truck was blue and yellow", public = true)

        mockMvc.perform(get("/v1/collections?query=truck").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("while a car and a truck crashed")))
    }

    @Test
    fun `can filter public collections by multiple subjects with commas`() {
        val frenchCollection = createCollectionWithTitle("French Collection for with Subjects")
        val germanCollection = createCollectionWithTitle("German Collection for with Subjects")
        val collectionWithoutSubjects = createCollectionWithTitle("My Collection for without Subjects")

        val frenchSubject = saveSubject("French")
        val germanSubject = saveSubject("German")

        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(frenchCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${frenchSubject.value}"]}""").asTeacher()
        )
        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(germanCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${germanSubject.value}"]}""").asTeacher()
        )

        updateCollectionToBePublic(frenchCollection)
        updateCollectionToBePublic(germanCollection)
        updateCollectionToBePublic(collectionWithoutSubjects)

        mockMvc.perform(
            get("/v1/collections?subject=${frenchSubject.value},${germanSubject.value}&public=true")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
    }

    @Test
    fun `can filter public collections by subjects`() {
        val frenchCollection = createCollectionWithTitle("My Collection for with Subjects")
        val unclassifiedCollection = createCollectionWithTitle("My Collection for without Subjects")

        val frenchSubject = saveSubject("French")

        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(frenchCollection)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"subjects": ["${frenchSubject.value}"]}""").asTeacher()
        )

        updateCollectionToBePublic(frenchCollection)
        updateCollectionToBePublic(unclassifiedCollection)

        mockMvc.perform(
            get("/v1/collections?subject=${frenchSubject.value}&projection=details&public=true")
                .asTeacher("teacher@gmail.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(1)))
    }

    private fun createCollectionForViewer(viewerId: String) =
        collectionRepository.createWithViewers(UserId("teacher@gmail.com"), "Viewer collection", listOf(viewerId))
}