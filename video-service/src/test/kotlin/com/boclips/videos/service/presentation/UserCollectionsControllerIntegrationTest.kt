package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class UserCollectionsControllerIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `users collections containing collections and bookmarks, sorted by last modified`() {
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
