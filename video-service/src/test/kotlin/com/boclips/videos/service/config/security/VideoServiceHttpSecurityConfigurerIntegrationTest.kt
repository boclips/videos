package com.boclips.videos.service.config.security

import com.boclips.videos.service.testsupport.*
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class VideoServiceHttpSecurityConfigurerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `everybody can access actuator without permissions`() {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `everybody  can access links without permissions`() {
        mockMvc.perform(get("/v1"))
                .andExpect(status().`is`(HttpStatus.OK.value()))

        mockMvc.perform(get("/v1/"))
                .andExpect(status().`is`(HttpStatus.OK.value()))
    }

    @Test
    fun `everybody can access interactions`() {
        mockMvc.perform(get("/v1/interactions"))
                .andExpect(status().`is`(HttpStatus.OK.value()))
    }

    @Test
    fun `everybody can access any endpoint with OPTIONS`() {
        mockMvc.perform(options("/v1/videos"))
                .andExpect(status().isOk)
        mockMvc.perform(options("/v1"))
                .andExpect(status().`is`(HttpStatus.OK.value()))
        mockMvc.perform(options("/v1/events/status"))
                .andExpect(status().`is`(HttpStatus.OK.value()))
    }

    @Test
    fun `everybody can access event status`() {
        mockMvc.perform(get("/v1/events/status"))
                .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `reporters can access event types`() {
        mockMvc.perform(get("/v1/events/no-search-results").asReporter())
                .andExpect(status().`is`(not401Or403()))
        mockMvc.perform(get("/v1/events/status").asReporter())
                .andExpect(status().`is`(not401Or403()))
        mockMvc.perform(post("/v1/events/playback").asReporter())
                .andExpect(status().`is`(not401Or403()))
        mockMvc.perform(post("/v1/events/no-search-results").asReporter())
                .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `get video does not require special roles`() {
        saveVideo(videoId = 123)

        mockMvc.perform(get("/v1/videos/123"))
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/123").asReporter())
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/123").asTeacher())
                .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `only teachers can get videos`() {
        saveVideo(videoId = 123)

        mockMvc.perform(get("/v1/videos?query=test"))
                .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos?query=test").asReporter())
                .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos?query=test").asTeacher())
                .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `remove videos requires a special role`() {
        saveVideo(videoId = 123)

        mockMvc.perform(delete("/v1/videos/123"))
                .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/videos/123").asTeacher())
                .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/videos/123").asReporter())
                .andExpect(status().isForbidden)

        mockMvc.perform(delete("/v1/videos/123").asOperator())
                .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `insert videos requires a special role`() {
        saveVideo(videoId = 123)

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
    fun `probe video existence requires a special role`() {
        saveVideo(videoId = 123)

        mockMvc.perform(head("/v1/content-partners/ted/videos/666"))
                .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/content-partners/ted/videos/666").asTeacher())
                .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/content-partners/ted/videos/666").asReporter())
                .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/content-partners/ted/videos/666").asOperator())
                .andExpect(status().isForbidden)

        mockMvc.perform(head("/v1/content-partners/ted/videos/666").asIngestor())
                .andExpect(status().`is`(not401Or403()))
    }

    @Test
    fun `only people with dedicated role can rebuild search index`() {
        mockMvc.perform(post("/v1/admin/actions/rebuild_search_index").asTeacher())
                .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/admin/actions/rebuild_search_index").asOperator())
                .andExpect(status().is2xxSuccessful)
    }
}

private fun not401Or403(): Matcher<Int> {
    return object : BaseMatcher<Int>() {
        override fun matches(item: Any?): Boolean {
            val statusActually = item as Int
            return statusActually !== 403 && statusActually !== 401
        }

        override fun describeTo(description: Description?) {

        }
    }
}