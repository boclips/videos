package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractVideoAccessRulesIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerContractsIntegrationTest : AbstractVideoAccessRulesIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns a 404 response when user accesses a video they don't have a contract for`() {
        val idUserIsNotContractedTo = saveVideo(title = "A non-contracted video")
        val idUserIsContractedTo = saveVideo(title = "Contracted video")

        createSelectedVideosContract(idUserIsContractedTo.value)

        mockMvc.perform(get("/v1/videos/$idUserIsNotContractedTo").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `returns the video if user has a access to it`() {
        val videoId = saveVideo(title = "Contracted video")

        createSelectedVideosContract(videoId.value)

        mockMvc.perform(get("/v1/videos/$videoId").asApiUser(email = "api-user@gmail.com"))
            .andExpect(status().isOk)
    }
}