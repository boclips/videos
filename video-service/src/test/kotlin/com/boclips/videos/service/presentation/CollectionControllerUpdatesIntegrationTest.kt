package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI

class CollectionControllerUpdatesIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `updates a collection`() {
        val firstVideoId = saveVideo(title = "first").value
        val secondVideoId = saveVideo(title = "second").value
        val thirdVideoId = saveVideo(title = "third").value

        val collectionId = createCollection().also {
            addVideo(it, firstVideoId)
        }

        val newTitle = "brave, new title"
        val newDescription = "brave, new description"

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "$newTitle",
                        "description": "$newDescription",
                        "videos": ["$secondVideoId", "$thirdVideoId"],
                        "attachment": {
                            "linkToResource": "http://google.com",
                            "description": "My new activity for a collection",
                            "type": "ACTIVITY"
                        }
                    }
                    """.trimIndent()
                )
                .asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.createdBy", equalTo("Teacher")))
            .andExpect(jsonPath("$.title", equalTo(newTitle)))
            .andExpect(jsonPath("$.description", equalTo(newDescription)))
            .andExpect(jsonPath("$.attachments[0].type", equalTo("ACTIVITY")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(2)))
            .andExpect(jsonPath("$.videos[*].id", containsInAnyOrder(secondVideoId, thirdVideoId)))
    }

    @Test
    fun `collections can be promoted by Boclips`() {
        val collectionId = createCollection()

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "promoted": "true"
                    }
                    """.trimIndent()
                )
                .asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.promoted", equalTo(true)))
    }

    @Test
    fun `collections cannot be promoted by non-Boclippers`() {
        val collectionId = createCollection()

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "promoted": "true"
                    }
                    """.trimIndent()
                )
                .asTeacher()
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(collectionId)))
            .andExpect(jsonPath("$.promoted", equalTo(false)))
    }

    @Test
    fun `add and remove video from a specific collection`() {
        val email = "teacher@gmail.com"
        val videoId = saveVideo(title = "a video title").value
        val collectionId =
            collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(email),
                    title = "My Special Collection",
                    createdByBoclips = false,
                    discoverable = false
                )
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
    fun `mark a collection as discoverable or not`() {
        val collectionId = createCollectionWithTitle("My Special Collection")

        assertCollectionIsNotDiscoverable(collectionId)
        updateCollectionToBeDiscoverable(collectionId)
        assertCollectionIsDiscoverable(collectionId)
    }

    @Test
    fun `change more than one property of a collection`() {
        val collectionId = createCollectionWithTitle("My Special Collection")

        assertCollectionIsNotDiscoverable(collectionId)
        assertCollectionName(collectionId, "My Special Collection")
        updateCollectionToBeDiscoverableAndRename(collectionId, "New Name")
        assertCollectionName(collectionId, "New Name")
        assertCollectionIsDiscoverable(collectionId)
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
                    Matchers.containsString("Age range min must be less than or equal to 19")
                )
            )
    }

    @Test
    fun `validates create collection request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": ""}""").asTeacher()
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.message",
                    Matchers.containsString("Title is required")
                )
            )
    }

    @Test
    fun `adds age range to the existing collection`() {
        val collectionId = createCollectionWithTitle("My Collection for ages")

        getCollection(collectionId)
            .andExpect(jsonPath("$.ageRange", Matchers.nullValue()))

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
            .andExpect(jsonPath("$.ageRange", Matchers.nullValue()))

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
                .content("""{"subjects": ["${frenchSubject.id.value}", "${germanSubject.id.value}"]}""").asTeacher()
        )
            .andExpect(status().isNoContent)

        updateCollectionToBeDiscoverableAndRename(collectionId, "My new shiny title")

        getCollection(collectionId).andExpect(jsonPath("$.subjects", hasSize<Any>(2)))
    }

    @Test
    fun `updates a collection as a user with VIEW_ANY_COLLECTION role`() {
        val collectionId = createCollection("a collection to test on")

        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "brave, new title"
                    }
                    """.trimIndent()
                )
                .asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `bookmark collections returns updated collection with not populated videos`() {
        val firstVideoId = saveVideo(title = "first").value
        val collectionId = createCollection("collection 1").also {
            addVideo(it, firstVideoId)
        }

        mockMvc.perform(patch("/v1/collections/$collectionId?bookmarked=true").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", Matchers.not(Matchers.emptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.mine", equalTo(false)))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.unbookmark.href").exists())
            .andExpect(jsonPath("$._links.bookmark").doesNotExist())
    }

    @Test
    fun `unbookmark collections returns updated collection with not populated videos`() {
        val firstVideoId = saveVideo(title = "first").value
        val collectionId = createCollection("collection 1").apply {
                bookmarkCollection(this, "notTheOwner@gmail.com")
            }.also {
            addVideo(it, firstVideoId)
        }

        mockMvc.perform(patch("/v1/collections/$collectionId?bookmarked=false").asTeacher(email = "notTheOwner@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", Matchers.not(Matchers.emptyString())))
            .andExpect(jsonPath("$.owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$.mine", equalTo(false)))
            .andExpect(jsonPath("$.title", equalTo("collection 1")))
            .andExpect(jsonPath("$.videos", hasSize<Any>(0)))

            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.bookmark.href").exists())
            .andExpect(jsonPath("$._links.unbookmark").doesNotExist())
    }

    @Test
    fun `highlighting collection from Boclips employees`() {
        val email = "terry@boclips.com"

        val collectionUrl = mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/collections").contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "a collection"}""")
                .asBoclipsEmployee(email)
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", Matchers.containsString("/collections/")))
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(get(collectionUrl).asBoclipsEmployee(email))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.createdBy", equalTo("Boclips")))
    }

    private fun removeVideo(collectionId: String, videoId: String) {
        mockMvc.perform(MockMvcRequestBuilders.delete(removeVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    private fun renameCollection(collectionId: String, title: String) {
        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content("""{"title": "$title"}""")
                .asTeacher()
        )
            .andExpect(status().isNoContent)
    }

    private fun updateCollectionToBeDiscoverableAndRename(collectionId: String, title: String) {
        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON)
                .content("""{"discoverable": "true", "title": "$title"}""").asTeacher()
        )
            .andExpect(status().isNoContent)
    }

    private fun assertCollectionIsDiscoverable(collectionId: String) {
        getCollection(collectionId).andExpect(jsonPath("$.discoverable", equalTo(true)))
    }

    private fun assertCollectionIsNotDiscoverable(collectionId: String) {
        getCollection(collectionId).andExpect(jsonPath("$.discoverable", equalTo(false)))
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
}
