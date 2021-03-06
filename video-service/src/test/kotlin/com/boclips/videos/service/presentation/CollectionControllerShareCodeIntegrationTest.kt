package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CollectionControllerShareCodeIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Nested
    inner class AuthenticatedUser {
        @Test
        fun `owner can access collection with share code and referer`() {
            val collectionId =
                createCollection(owner = "12345", title = "Some Private Collection", discoverable = false)
            usersClient.add(
                UserResourceFactory.sample(
                    id = "12345",
                    shareCode = "VALID"
                )
            )

            mockMvc.perform(get("/v1/collections/$collectionId?referer=12345&shareCode=VALID").asTeacher(email = "12345"))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(collectionId)))
                .andExpect(jsonPath("$.title", equalTo("Some Private Collection")))
        }

        @Test
        fun `authenticated user can access collection with share code and referer`() {
            val collectionId =
                createCollection(owner = "12345", title = "Some Private Collection", discoverable = false)
            usersClient.add(
                UserResourceFactory.sample(
                    id = "12345",
                    shareCode = "VALID"
                )
            )

            mockMvc.perform(get("/v1/collections/$collectionId?referer=12345&shareCode=VALID").asTeacher(email = "another-teacher"))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(collectionId)))
                .andExpect(jsonPath("$.title", equalTo("Some Private Collection")))
        }

        @Test
        fun `authenticated users can access collections without share codes`() {
            val collectionId = createCollection(owner = "12345", title = "Some Collection", discoverable = false)

            mockMvc.perform(get("/v1/collections/$collectionId").asTeacher())
                .andExpect(status().isOk)
        }
    }

    @Nested
    inner class NonAuthenticatedUser {

        @Test
        fun `providing a valid shareCode and referer returns a collection resource`() {
            val collectionId =
                createCollection(title = "Some discoverable Collection", discoverable = true, owner = "12345")
            usersClient.add(
                UserResourceFactory.sample(
                    id = "12345",
                    shareCode = "TEST"
                )
            )

            mockMvc.perform(get("/v1/collections/$collectionId?referer=12345&shareCode=TEST"))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(collectionId)))
                .andExpect(jsonPath("$.title", equalTo("Some discoverable Collection")))
        }

        @Test
        fun `providing an invalid shareCode and valid referer returns forbidden error code`() {
            val collectionId = createCollection(title = "Some discoverable Collection", discoverable = true)
            usersClient.add(
                UserResourceFactory.sample(
                    id = "12345",
                    shareCode = "TEST"
                )
            )

            mockMvc.perform(get("/v1/collections/$collectionId?referer=12345&shareCode=INVALID"))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `not providing neigher shareCode nor referer returns forbidden error code`() {
            val collectionId = createCollection(title = "Some discoverable Collection", discoverable = true)
            usersClient.add(
                UserResourceFactory.sample(
                    id = "12345",
                    shareCode = "TEST"
                )
            )

            mockMvc.perform(get("/v1/collections/$collectionId"))
                .andExpect(status().isForbidden)
        }
    }
}
