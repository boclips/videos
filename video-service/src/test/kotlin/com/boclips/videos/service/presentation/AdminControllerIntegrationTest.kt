package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.VideosToAnalyse
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var videosToAnalyse: VideosToAnalyse

    @Autowired
    lateinit var messageCollector: MessageCollector

    @BeforeEach
    fun setUp() {
        saveVideo()
    }

    @Test
    fun `rebuildSearchIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_search_index").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `rebuildSearchIndex returns 200 OK when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_search_index").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `buildLegacySearchIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/build_legacy_search_index").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `buildLegacySearchIndex returns 200 OK when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/build_legacy_search_index").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `refreshVideoDuration returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/refresh_video_durations").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `refreshVideoDuration returns 200 OK when user is allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/refresh_video_durations").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `analyse video enqueues videos for analysis`() {
        val assetId = saveVideo()
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$assetId").asOperator())
            .andExpect(MockMvcResultMatchers.status().isAccepted)

        val message = messageCollector.forChannel(videosToAnalyse.output()).poll()

        assertThat(message.payload).isEqualTo(assetId.value)
    }

    @Test
    fun `analyse video returns 403 when user is not allowed`() {
        val assetId = saveVideo()
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$assetId").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)

        val message = messageCollector.forChannel(videosToAnalyse.output()).poll()

        assertThat(message).isNull()
    }
}