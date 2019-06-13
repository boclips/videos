package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        saveVideo()
    }

    @Test
    fun `rebuildVideoIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_video_index").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `rebuildVideoIndex returns 200 OK when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_video_index").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `rebuildCollectionIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_collection_index").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `rebuildCollectionIndex returns 200 OK when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_collection_index").asOperator())
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
    fun `refresh playbacks returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/refresh_playbacks").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `refresh playbacks returns 200 OK when user is allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/refresh_playbacks").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `refresh only youtube playback information`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/refresh_playbacks?source=youtube").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `refresh bad playback information returns 400`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/refresh_playbacks?source=BAD").asOperator())
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `update youtube channel returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/update_youtube_channel_names").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `analyse video publishes events`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "123"))
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId?language=en_US").asOperator())
            .andExpect(MockMvcResultMatchers.status().isAccepted)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message.payload.toString()).contains(videoId.value)
        assertThat(message.payload.toString()).contains("https://download/video-entry-123.mp4")
        assertThat(message.payload.toString()).contains("en_US")
    }

    @Test
    fun `analyse video returns 400 for youtube videos`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "123"))
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId").asOperator())
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `analyse video returns 403 when user is not allowed`() {
        val videoId = saveVideo()
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `analyse content partner videos publishes events`() {
        saveVideo(contentProvider = "Ted")
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_videos?contentPartner=Ted&language=es_ES").asOperator())
            .andExpect(MockMvcResultMatchers.status().isAccepted)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message).isNotNull
        assertThat(message.payload.toString()).contains("es_ES")
    }

    @Test
    fun `analyse content partner videos returns 400 for YouTube channels`() {
        saveVideo(
            contentProvider = "TheYoutuber",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "id")
        )
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_videos?contentPartner=TheYoutuber").asOperator())
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `analyse content partner videos returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_videos?contentPartner=Ted").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `classify content partner videos publishes events`() {
        saveVideo(contentProvider = "AContentPartner")

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/classify_videos?contentPartner=AContentPartner").asOperator())
            .andExpect(MockMvcResultMatchers.status().isAccepted)
    }

    @Test
    fun `classify content partner videos returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/classify_videos?contentPartner=AContentPartner").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNull()
    }
}
