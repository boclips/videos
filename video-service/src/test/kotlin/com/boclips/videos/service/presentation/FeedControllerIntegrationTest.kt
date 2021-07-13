package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.asApiUser
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZonedDateTime

class FeedControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `can fetch all videos`() {
        val video1 = saveVideo(title = "1")
        val video2 = saveVideo(title = "2")
        val video3 = saveVideo(title = "3")

        mockMvc.perform(get("/v1/feed/videos").asApiUser())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(3)))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[*].id",
                    containsInAnyOrder(
                        video1.value,
                        video2.value,
                        video3.value
                    )
                )
            )
    }

    @Test
    fun `can page through using next link`() {
        saveVideo(title = "1")
        saveVideo(title = "2")
        val video3 = saveVideo(title = "3")

        val nextLink = mockMvc.perform(get("/v1/feed/videos?size=2").asApiUser())
            .andReturnLink("next")

        mockMvc.perform(get(nextLink.expand()).asApiUser())
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(
                jsonPath("$._embedded.videos[*].id", containsInAnyOrder(video3.value))
            )
    }

    @Test
    fun `can filter for videos updated since a particular time`() {
        createDatedVideo("exclude-me", "1993-12-31T23:59:59Z")
        createDatedVideo("midnight", "1994-01-01T00:00:00Z")
        createDatedVideo("mellow-gold", "1994-03-01T00:00:00Z")
        createDatedVideo("downward-spiral", "1994-03-08T00:00:00Z")

        mockMvc.perform(get("/v1/feed/videos?updated_as_of=1994-01-01").asApiUser())
            .andExpect(
                jsonPath(
                    "$._embedded.videos[*].title",
                    equalTo(
                        listOf(
                            "midnight",
                            "mellow-gold",
                            "downward-spiral"
                        )
                    )
                )
            )
    }

    @Test
    fun `copes with badly formatted updated start date`() {
        createDatedVideo("exclude-me", "1993-12-31T23:59:59Z")
        createDatedVideo("midnight", "1994-01-01T00:00:00Z")
        createDatedVideo("mellow-gold", "1994-03-01T00:00:00Z")
        createDatedVideo("downward-spiral", "1994-03-08T00:00:00Z")

        mockMvc.perform(get("/v1/feed/videos?updated_as_of=1994-01-01T00:00:00Z").asApiUser())
            .andExpect(status().is4xxClientError)
            .andExpectApiErrorPayload()
    }

    private fun createDatedVideo(title: String, updatedAt: String) {
        videoRepository.create(
            TestFactories.createVideo(
                title = title, updatedAt = ZonedDateTime.parse(updatedAt)
            )
        )
    }

    @Test
    fun `next link does not exist when no results return`() {
        mockMvc.perform(get("/v1/feed/videos?size=2").asApiUser())
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(0)))
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `returns a bad request when cursor id is invalid`() {
        mockMvc.perform(get("/v1/feed/videos?cursorId=YOU_SHALL_NOT_PASS").asApiUser())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `returns a bad request when size is too large`() {
        mockMvc.perform(get("/v1/feed/videos?size=1001").asApiUser())
            .andExpect(status().isBadRequest)
    }
}
