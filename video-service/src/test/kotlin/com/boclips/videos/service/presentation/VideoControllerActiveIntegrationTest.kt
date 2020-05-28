package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoDuplicationService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class VideoControllerActiveIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var videoDuplicationService: VideoDuplicationService

    @Test
    fun `search by query only shows active videos`() {
        val oldVideoId = saveVideo(title = "dogs eat a lot")

        val newVideoId = saveVideo(title = "dogs eat a lot")

        videoDuplicationService.markDuplicate(oldVideoId, newVideoId)

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos?query=dogs").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.videos", Matchers.hasSize<Any>(1)))
    }

    @Test
    fun `get by id only shows any video`() {
        val oldVideoId = saveVideo(
            title = "dogs eat a lot"
        ).value

        val newVideoId = saveVideo(
            title = "dogs eat a lot"
        ).value

        videoDuplicationService.markDuplicate(VideoId(oldVideoId), VideoId(newVideoId))

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${oldVideoId}").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}