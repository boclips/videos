package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.boclips.videos.service.testsupport.asApiUser
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CollectionsControllerAccessRulesIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `returns collections user is contracted to when they have any SelectedContent contracts`() {
        val firstCollection = createCollection(title = "My First Collection", discoverable = true)
        val secondCollection = createCollection(title = "My Second Collection", discoverable = false)
        createCollection(title = "My Third Collection", discoverable = false)

        createIncludedCollectionsAccessRules("api-user@gmail.com", firstCollection, secondCollection)

        mockMvc.perform(get("/v1/collections").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(2)))
            .andExpect(
                jsonPath(
                    "$._embedded.collections[*].title",
                    containsInAnyOrder("My First Collection", "My Second Collection")
                )
            )
    }

    @Test
    fun `returns collections with shallow video information by default`() {
        val collectionId = createCollection(title = "A Collection", discoverable = false)
        addVideo(collectionId, saveVideo(title = "a video title", contentProvider = "A content provider").value)

        createIncludedCollectionsAccessRules("api-user@gmail.com", collectionId)

        mockMvc.perform(get("/v1/collections").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$._embedded.collections", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("A Collection")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].createdBy", nullValue()))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].playback", nullValue()))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", endsWith(collectionId)))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.details.href").exists())
            .andExpect(jsonPath("$._links.list.href").exists())
    }

    @Test
    fun `returns collections with deep video information when details projection is used`() {
        val collectionId = createCollection(title = "A Collection", discoverable = false)
        addVideo(collectionId, saveVideo(title = "a video title", contentProvider = "A content provider").value)
        createIncludedCollectionsAccessRules("api-user@gmail.com", collectionId)

        mockMvc.perform(get("/v1/collections?projection=details").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.collections", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].id", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0].owner", equalTo("teacher@gmail.com")))
            .andExpect(jsonPath("$._embedded.collections[0].title", equalTo("A Collection")))
            .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].title", equalTo("a video title")))
            .andExpect(jsonPath("$._embedded.collections[0].videos[0].createdBy", equalTo("A content provider")))
            .andExpect(
                jsonPath(
                    "$._embedded.collections[0].videos[0].playback._links.thumbnail.href",
                    not(isEmptyString())
                )
            )
            .andExpect(jsonPath("$._embedded.collections[0].videos[0]._links.self.href", not(isEmptyString())))
            .andExpect(jsonPath("$._embedded.collections[0]._links.self.href", endsWith(collectionId)))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.details.href").exists())
            .andExpect(jsonPath("$._links.list.href").exists())
    }

    @Test
    fun `returns empty result set when user has an empty SelectedContent contract`() {
        createIncludedCollectionsAccessRules("1")

        mockMvc.perform(get("/v1/collections").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.collections", hasSize<Any>(0)))
    }

    @Nested
    inner class VideoContracts {
        @Test
        fun `limits videos returned on a collection search results to contracted ones for public collections`() {
            val collectionId = createCollection(title = "A Collection", discoverable = true)

            val contractedVideoId = saveVideo()
            val nonContractedVideoId = saveVideo()

            addVideo(collectionId, contractedVideoId.value)
            addVideo(collectionId, nonContractedVideoId.value)

            addAccessToVideoIds("api-user@gmail.com", contractedVideoId.value)

            mockMvc.perform(get("/v1/collections?projection=details").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.collections[0].videos[0].id", equalTo(contractedVideoId.value)))

            mockMvc.perform(get("/v1/collections?projection=list").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.collections[0].videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.collections[0].videos[0].id", equalTo(contractedVideoId.value)))
        }

        @Test
        fun `collection access is transitive to their contained videos`() {
            val collectionId = createCollection(title = "A Collection", discoverable = false)
            val contractedVideoId = saveVideo()
            val nonContractedVideoId = saveVideo()
            addVideo(collectionId, contractedVideoId.value)

            createIncludedCollectionsAccessRules("api-user@gmail.com", collectionId)

            mockMvc.perform(get("/v1/collections/$collectionId?projection=details").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$.videos[0].id", equalTo(contractedVideoId.value)))

            mockMvc.perform(get("/v1/collections/$collectionId?projection=list").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$.videos[0].id", equalTo(contractedVideoId.value)))
        }
    }
}
