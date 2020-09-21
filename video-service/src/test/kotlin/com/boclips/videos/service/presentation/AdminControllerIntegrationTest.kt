package com.boclips.videos.service.presentation

import com.boclips.eventbus.events.collection.CollectionBroadcastRequested
import com.boclips.eventbus.events.video.CleanUpDeactivatedVideoRequested
import com.boclips.eventbus.events.video.RetryVideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.users.api.response.accessrule.ContentPackageResource
import com.boclips.videos.api.response.channel.DistributionMethodResource
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        saveVideo()
    }

    @Test
    fun `broadcast video events`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_videos").asOperator())
            .andExpect(status().isOk)
    }

    @Test
    fun `broadcast video events returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_videos").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `broadcast collection events`() {
        saveCollection()

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_collections").asOperator())
            .andExpect(status().isOk)

        assertThat(fakeEventBus.getEventsOfType(CollectionBroadcastRequested::class.java)).isNotEmpty
    }

    @Test
    fun `broadcast collection events returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_collections").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `broadcast channel events`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_channels").asOperator())
            .andExpect(status().isOk)
    }

    @Test
    fun `broadcast channel events returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_channels").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `broadcast contract events`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_contracts").asOperator())
            .andExpect(status().isOk)
    }

    @Test
    fun `broadcast contract events returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/broadcast_contracts").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `analyse video publishes events`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "entry-123"))
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId?language=en_US").asOperator()
        )
            .andExpect(status().isAccepted)

        val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

        assertThat(event.videoId).contains(videoId.value)
        assertThat(event.videoUrl).contains("https://download.com/entryId/entry-123/format/download")
        assertThat(event.language.toLanguageTag()).isEqualTo("en-US")
    }

    @Test
    fun `retry analyse video publishes event`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "entry-123"))
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId?language=en_US&retry=true")
                .asOperator()
        )
            .andExpect(status().isAccepted)

        val event = fakeEventBus.getEventOfType(RetryVideoAnalysisRequested::class.java)

        assertThat(event.videoId).contains(videoId.value)
        assertThat(event.videoUrl).contains("https://download.com/entryId/entry-123/format/download")
        assertThat(event.language.toLanguageTag()).isEqualTo("en-US")
    }

    @Test
    fun `analyse video returns 400 for youtube videos`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "123"))
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId").asOperator())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `analyse video returns 403 when user is not allowed`() {
        val videoId = saveVideo()
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_video/$videoId").asTeacher())
            .andExpect(status().isForbidden)

        assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
    }

    @Test
    fun `analyse content partner videos publishes events`() {
        saveVideo(contentProvider = "Ted")
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/admin/actions/analyse_videos?contentPartner=Ted&language=es_ES")
                .asOperator()
        )
            .andExpect(status().isAccepted)

        val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

        assertThat(event.language.toLanguageTag()).isEqualTo("es-ES")
    }

    @Test
    fun `analyse content partner videos returns 400 for YouTube channels`() {
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "id"),
            contentProvider = "TheYoutuber"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/admin/actions/analyse_videos?contentPartner=TheYoutuber").asOperator()
        )
            .andExpect(status().isBadRequest)

        assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
    }

    @Test
    fun `analyse content partner videos returns 403 when user is not allowed`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/analyse_videos?contentPartner=Ted").asTeacher())
            .andExpect(status().isForbidden)

        assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
    }

    @Test
    fun `classify content partner videos publishes events`() {
        saveVideo(contentProvider = "AContentPartner")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/admin/actions/classify_videos?contentPartner=AContentPartner").asOperator()
        )
            .andExpect(status().isAccepted)
    }

    @Test
    fun `classify content partner videos returns 403 when user is not allowed`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/admin/actions/classify_videos?contentPartner=AContentPartner").asTeacher()
        )
            .andExpect(status().isForbidden)

        assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
    }

    @Test
    fun `clean deactivated videos publishes events for deactivated videos`() {
        val oldVideoId = saveVideo(contentProvider = "TED", title = "Duplicate video")
        saveVideo(contentProvider = "TED", title = "Duplicate video")

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v1/admin/actions/clean_deactivated_videos")
                .asOperator()
        )
            .andExpect(status().isAccepted)

        val event = fakeEventBus.getEventOfType(CleanUpDeactivatedVideoRequested::class.java)

        assertThat(event.videoId).isEqualTo(oldVideoId.value)
    }

    @Test
    fun `cannot access content package video id endpoint without operator permissions`() {
        mockMvc.perform(
            get("/v1/admin/actions/get_all_video_ids_for_content_package/arbitrary")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `can get all videos with a permissive content package`() {
        val v1 = saveVideo(
            distributionMethods = setOf(
                DistributionMethodResource.STREAM
            )
        )
        val v2 = saveVideo(
            distributionMethods = setOf(
                DistributionMethodResource.DOWNLOAD
            )
        )
        val v3 = saveVideo(
            distributionMethods = setOf(
                DistributionMethodResource.STREAM,
                DistributionMethodResource.DOWNLOAD
            )
        )

        contentPackagesClient.add(
            ContentPackageResource(
                id = "package-id",
                name = "package name",
                accessRules = listOf(
                    AccessRuleResource.IncludedDistributionMethods(
                        id = "rule1",
                        name = "name1",
                        distributionMethods = listOf(
                            "DOWNLOAD, STREAM"
                        )
                    )
                ),
                _links = mapOf()
            )
        )

        mockMvc.perform(
            get("/v1/admin/actions/get_all_video_ids_for_content_package/package-id")
                .asOperator()
        )
            .andExpect(status().isAccepted)
            .andExpect(
                content().json(
                    """
                {
                    "videoIds": ["$v1", "$v2", "$v3"]
                }
            """.trimIndent()
                )
            )
    }

    @Test
    fun `can get no videos with a restrictive content package`() {
        TODO()
    }

    @Test
    fun `can get some videos with a specific content package`() {
        TODO()
    }
}
