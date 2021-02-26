package com.boclips.videos.service.config.security

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asReporter
import com.boclips.videos.service.testsupport.asTeacher
import com.boclips.videos.service.testsupport.asUserWithRoles
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WebSecurityConfigIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `everybody can access actuator without permissions`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `everybody can access links without permissions`() {
        mockMvc.perform(get("/v1"))
            .andExpect(status().`is`(HttpStatus.OK.value()))

        mockMvc.perform(get("/v1/"))
            .andExpect(status().`is`(HttpStatus.OK.value()))
    }

    @Test
    fun `everybody can access any endpoint with OPTIONS`() {
        mockMvc.perform(options("/v1/videos"))
            .andExpect(status().isOk)

        mockMvc.perform(options("/v1"))
            .andExpect(status().`is`(HttpStatus.OK.value()))
    }

    @Test
    fun `get video does not require special roles`() {
        val videoId = saveVideo()

        mockMvc.perform(get("/v1/videos/${videoId.value}"))
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/${videoId.value}").asReporter())
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/${videoId.value}").asTeacher())
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `anyone can download transcripts`() {
        val videoId = saveVideo()

        mockMvc.perform(get("/v1/videos/${videoId.value}/transcript"))
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `only boclipers can download video assets`() {
        val videoId = saveVideo()

        mockMvc.perform(get("/v1/videos/${videoId.value}/assets"))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos/${videoId.value}/assets").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos/${videoId.value}/assets").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos/${videoId.value}/assets").asBoclipsEmployee())
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `only teachers can get videos`() {
        saveVideo()

        mockMvc.perform(get("/v1/videos?query=test"))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos?query=test").asReporter())
            .andExpect(status().isForbidden)

        val organisation = organisationsClient.add(OrganisationResourceFactory.sample())
        usersClient.add(
            UserResourceFactory.sample(
                id = "the@teacher.com",
                organisation = OrganisationResourceFactory.sampleDetails(id = organisation.id)
            )
        )
        mockMvc.perform(get("/v1/videos?query=test").asTeacher("the@teacher.com"))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `looking up by slash respects videos rule`() {
        mockMvc.perform(get("/v1/videos/"))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos/").asReporter())
            .andExpect(status().isForbidden)

        val organisation = organisationsClient.add(OrganisationResourceFactory.sample())
        usersClient.add(
            UserResourceFactory.sample(
                id = "the@teacher.com",
                organisation = OrganisationResourceFactory.sampleDetails(id = organisation.id)
            )
        )
        mockMvc.perform(get("/v1/videos/").asTeacher("the@teacher.com"))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `teachers cannot update videos`() {
        val videoId = saveVideo().value

        mockMvc.perform(patch("/v1/videos/$videoId").content("{}").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `only teachers can rate`() {
        val videoId = saveVideo()

        mockMvc.perform(patch("/v1/videos/${videoId.value}?rating=3"))
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/videos/${videoId.value}?rating=3").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/videos/${videoId.value}?rating=3").asTeacher())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `teachers can access their collections`() {
        mockMvc.perform(get("/v1/collections?projection=details&public=true&page=0&size=1"))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/collections?projection=details&public=true&page=0&size=1").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/collections?projection=details&public=true&page=0&size=1").asTeacher())
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `teachers can rename their collections`() {
        mockMvc.perform(patch("/v1/collections/53fbf4615c3b9f41c381b6a3"))
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/collections/53fbf4615c3b9f41c381b6a3").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/collections/53fbf4615c3b9f41c381b6a3").asTeacher())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `teachers can delete their collections`() {
        mockMvc.perform(delete("/v1/collections/53fbf4615c3b9f41c381b6a3"))
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/collections/53fbf4615c3b9f41c381b6a3").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/collections/53fbf4615c3b9f41c381b6a3").asTeacher())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `teachers can create their collections`() {
        mockMvc.perform(post("/v1/collections"))
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/collections").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/collections").asTeacher())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `teachers can access specific collections`() {
        mockMvc.perform(get("/v1/collections/53fbf4615c3b9f41c381b6a3").asTeacher())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `remove videos requires a special role`() {
        val videoId = saveVideo()

        mockMvc.perform(delete("/v1/videos/${videoId.value}"))
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/videos/${videoId.value}").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/videos/${videoId.value}").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/videos/${videoId.value}").asOperator())
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `insert videos requires a special role`() {
        saveVideo()

        mockMvc.perform(post("/v1/videos"))
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos").asOperator())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos").asIngestor())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `bulk upload videos requires a special role`() {
        saveVideo()

        mockMvc.perform(patch("/v1/videos"))
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/videos").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/videos").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/videos").asIngestor())
            .andExpect(status().isForbidden)

        mockMvc.perform(patch("/v1/videos").asOperator())
            .andExpect(status().`is`(not401Or403()))

        mockMvc.perform(patch("/v1/videos").asBoclipsEmployee())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `search enabled and disabled videos requires a special role`() {
        saveVideo()

        mockMvc.perform(post("/v1/videos/search"))
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos/search").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos/search").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos/search").asOperator())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos/search").asIngestor())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/videos/search").asBoclipsEmployee())
            .andExpect(status().`is`(not401Or403()))

        mockMvc.perform(post("/v1/videos/search").asUserWithRoles(UserRoles.VIEW_DISABLED_VIDEOS))
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `probe video existence requires a special role`() {
        mockMvc.perform(head("/v1/channels/ted/videos/666"))
            .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/channels/ted/videos/666").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/channels/ted/videos/666").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/channels/ted/videos/666").asOperator())
            .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/channels/ted/videos/666").asIngestor())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `probe video existence posting requires a special role`() {
        mockMvc.perform(post("/v1/channels/ted/videos/search"))
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/channels/ted/videos/search").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/channels/ted/videos/search").asReporter())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/channels/ted/videos/search").asOperator())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/channels/ted/videos/search").asIngestor())
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `channels signed url can only be created with special role`() {
        mockMvc.perform(post("/v1/channels/signed-upload-link"))
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/channels/signed-upload-link").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/v1/channels/signed-upload-link").with(
                user("user")
                    .roles(UserRoles.INSERT_CHANNELS)
            )
        )
            .andExpect(status().`is`(not401Or403()))

        mockMvc.perform(
            post("/v1/channels/signed-upload-link").with(
                user("user")
                    .roles(UserRoles.UPDATE_CHANNELS)
            )
        )
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `everybody can access subjects without permissions`() {
        mockMvc.perform(get("/v1/subjects"))
            .andExpect(status().`is`(HttpStatus.OK.value()))
    }

    @Test
    fun `everybody can access a subject without permissions`() {
        mockMvc.perform(get("/v1/subjects/53fbf4615c3b9f41c381b6a3"))
            .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `only users with dedicated role can create and fetch legal restrictions`() {
        mockMvc.perform(post("/v1/legal-restrictions?text=text").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/legal-restrictions?text=text").asBoclipsEmployee())
            .andExpect(status().isCreated)

        mockMvc.perform(get("/v1/legal-restrictions").asTeacher())
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/legal-restrictions").asBoclipsEmployee())
            .andExpect(status().isOk)
    }
}

private fun not401Or403(): Matcher<Int> {
    return object : BaseMatcher<Int>() {
        override fun matches(item: Any?): Boolean {
            val statusActually = item as Int
            return statusActually != 403 && statusActually != 401
        }

        override fun describeTo(description: Description?) {
        }
    }
}
