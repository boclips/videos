package com.boclips.videos.service.config.security

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asReporter
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Test
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
                .andExpect(status().`is`(isNot401Or403()))
    }

    @Test
    fun `reporters can access event types`() {
        mockMvc.perform(get("/v1/events/no-search-results").asReporter())
                .andExpect(status().`is`(isNot401Or403()))
        mockMvc.perform(get("/v1/events/status").asReporter())
                .andExpect(status().`is`(isNot401Or403()))
        mockMvc.perform(post("/v1/events/playback").asReporter())
                .andExpect(status().`is`(isNot401Or403()))
        mockMvc.perform(post("/v1/events/no-search-results").asReporter())
                .andExpect(status().`is`(isNot401Or403()))
    }

    @Test
    fun `get videos as different users`() {
        saveVideo(videoId = 123,
                playbackId = PlaybackId(playbackId = "ref-id-1", playbackProviderType = PlaybackProviderType.KALTURA),
                title = "powerful video about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = "00:01:00",
                contentProvider = "cp")

        mockMvc.perform(get("/v1/videos?query=test"))
                .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos/123"))
                .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos?query=test").asReporter())
                .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos/123").asReporter())
                .andExpect(status().isForbidden)

        mockMvc.perform(get("/v1/videos?query=test").asTeacher())
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/123").asTeacher())
                .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `remove videos requires a special role`() {
        saveVideo(videoId = 123,
                playbackId = PlaybackId(playbackId = "ref-id-1", playbackProviderType = PlaybackProviderType.KALTURA),
                title = "powerful video about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = "00:01:00",
                contentProvider = "cp")

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
    fun `only people with dedicated role can rebuild search index`() {
        mockMvc.perform(post("/v1/admin/actions/rebuild_search_index").asTeacher())
                .andExpect(status().isForbidden)

        mockMvc.perform(post("/v1/admin/actions/rebuild_search_index").asOperator())
                .andExpect(status().is2xxSuccessful)
    }
}

private fun isNot401Or403(): Matcher<Int> {
    return object : BaseMatcher<Int>() {
        override fun matches(item: Any?): Boolean {
            val statusActually = item as Int
            return statusActually !== 403 && statusActually !== 401
        }

        override fun describeTo(description: Description?) {

        }
    }
}