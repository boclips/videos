package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Duration

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        saveVideo(videoId = 123,
                playbackId = PlaybackId(value = "ref-id-1", type = PlaybackProviderType.KALTURA),
                title = "powerful asset about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = Duration.ofSeconds(23),
                contentProvider = "cp"
        )

        saveVideo(videoId = 124,
                playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
                title = "elaphants took out jobs",
                description = "it's a asset from youtube",
                date = "2017-02-11",
                duration = Duration.ofSeconds(56),
                contentProvider = "cp2"
        )
    }

    @Test
    fun `returns Kaltura videos when query matches`() {
        mockMvc.perform(get("/v1/videos?query=powerful").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo("123")))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful asset about elephants")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT23S")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.streamUrl", equalTo("https://stream/mpegdash/asset-entry-123.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-123.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/123")))

                .andExpect(jsonPath("$.page.size", Matchers.equalTo(100)))
                .andExpect(jsonPath("$.page.totalElements", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.page.totalPages", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.page.number", Matchers.equalTo(0)))
                .andExpect(jsonPath("$._links.prev").doesNotExist())
                .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `returns Youtube videos when query matches`() {
        mockMvc.perform(get("/v1/videos?query=jobs").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo("124")))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("elaphants took out jobs")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("it's a asset from youtube")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2017-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].contentProvider", equalTo("cp2")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("YOUTUBE")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT56S")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.youtubeId", equalTo("yt-id-124")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.thumbnailUrl", equalTo("https://youtube.com/thumb/yt-id-124.png")))
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/124")))
    }

    @Test
    fun `returns empty videos array when nothing matches`() {
        mockMvc.perform(get("/v1/videos?query=whatdohorseseat").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(0)))
    }

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos").asTeacher())
                .andExpect(status().`is`(400))
    }

    @Test
    fun `returns 200 for valid video`() {
        mockMvc.perform(get("/v1/videos/123").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo("123")))
                .andExpect(jsonPath("$.title", equalTo("powerful asset about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.contentProvider", equalTo("cp")))
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
                .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/mpegdash/asset-entry-123.mp4")))
                .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-123.mp4")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/123")))
    }

    @Test
    fun `returns 404 for inexistent video`() {
        mockMvc.perform(get("/v1/videos/9999").asTeacher())
                .andExpect(status().`is`(404))
    }

    @Test
    fun `returns 200 when video is deleted`() {
        mockMvc.perform(delete("/v1/videos/123").asOperator())
                .andExpect(status().`is`(200))
    }

    @Test
    fun `returns correlation id`() {
        mockMvc.perform(get("/v1/videos?query=powerful").header("X-Correlation-ID", "correlation-id").asTeacher())
                .andExpect(status().isOk)
                .andExpect(header().string("X-Correlation-ID", "correlation-id"))
    }

    @Test
    fun `records search events`() {
        mockMvc.perform(get("/v1/videos?query=bugs").header("X-Correlation-ID", "correlation-id").asBoclipsEmployee())
                .andExpect(status().isOk)

        val searchEvent = eventService.latestInteractions().last()
        assertThat(searchEvent.description).startsWith("Search for 'bugs'")
        assertThat(searchEvent.user.boclipsEmployee).isTrue()
    }

    @Test
    fun `create new video`() {

        fakeKalturaClient.addMediaEntry(TestFactories.createMediaEntry(id = "entry-$123", referenceId = "abc1", duration = Duration.ofMinutes(1)))

        val content = """
            {
                "provider": "AP",
                "providerVideoId": "1",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "contentType": "INSTRUCTIONAL_CLIPS",
                "playbackId": "abc1",
                "playbackProvider": "KALTURA"
            }
        """.trimIndent()

        val createdResourceUrl = mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated)
                .andReturn().response.getHeader("Location")

        mockMvc.perform(get(createdResourceUrl!!).asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title", equalTo("AP title")))
    }

    @Test
    fun `teachers cannot create videos`() {
        mockMvc.perform(post("/v1/videos").asTeacher().contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden)
    }

    @Test
    fun `return BAD_REQUEST when content is invalid`() {
        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest)
    }

}

