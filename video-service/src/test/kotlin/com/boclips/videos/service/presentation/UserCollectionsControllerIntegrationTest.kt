package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class UserCollectionsControllerIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `collections containing collections and bookmarks, sorted by last modified`() {
        val teacher = "teacher"
        val stranger = "stranger"

        createCollection(title = "mine", discoverable = true, owner = teacher)
        createCollection(title = "strangers", discoverable = true, owner = stranger)
        createCollection(title = "another collection", discoverable = true, owner = teacher)
        createCollection(title = "bookmarked", discoverable = true, owner = stranger).apply {
            bookmarkCollection(this, teacher)
        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/users/teacher/collections?sort_by=UPDATED_AT").asTeacher(teacher)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.collections", Matchers.hasSize<Any>(3)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$._embedded.collections[0].title",
                    Matchers.equalTo("bookmarked")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$._embedded.collections[1].title",
                    Matchers.equalTo("another collection")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.collections[2].title", Matchers.equalTo("mine")))
    }

    @Test
    fun `parent collections contain subCollections`() {
        val owner = UserId("stranger")

        val parentCollection = collectionRepository.create(
            CreateCollectionCommand(
                owner = owner,
                title = "Parent collections",
                createdByBoclips = false,
                discoverable = true
            )
        ).id

        val collectionUnit = collectionRepository.create(
            CreateCollectionCommand(
                owner = owner,
                title = "Collection unit",
                createdByBoclips = false,
                discoverable = true
            )
        ).id

        collectionRepository.update(
            CollectionUpdateCommand.AddCollectionToCollection(
                collectionId = parentCollection,
                subCollectionId = collectionUnit,
                user = UserFactory.sample(id = owner.value)
            )
        )

        bookmarkCollection(parentCollection.value, "teacher")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/users/teacher/collections?sort_by=UPDATED_AT").asTeacher("teacher")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.collections", Matchers.hasSize<Any>(1)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$._embedded.collections[0].subCollections",
                    Matchers.hasSize<Any>(1)
                )
            )
    }

    @Test
    fun `users collections filtering out bookmarks`() {
        createCollection(title = "mine", discoverable = true, owner = "teacher")
        createCollection(title = "bookmarked", discoverable = true, owner = "stranger").apply {
            bookmarkCollection(this, "teacher")
        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/users/teacher/collections?bookmarked=false").asTeacher("teacher")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.collections", Matchers.hasSize<Any>(1)))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$._embedded.collections[0].title", Matchers.equalTo("mine"))
            )
    }
}
