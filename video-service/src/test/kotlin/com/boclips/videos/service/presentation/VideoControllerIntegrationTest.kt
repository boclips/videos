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
                playbackId = PlaybackId(value = "ref-id-123", type = PlaybackProviderType.KALTURA),
                title = "powerful asset about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = Duration.ofSeconds(23),
                contentProvider = "cp"
        )

        saveVideo(videoId = 124,
                playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
                title = "elephants took out jobs",
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
                .andExpect(jsonPath("$._embedded.videos[0].contentPartner", equalTo("cp")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
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
    fun `filters out non educational results when filter param set`() {
        val excludedVideoId = 999L
        saveVideo(videoId = excludedVideoId, title = "Non educational video about elephants")

        mockMvc.perform(get("/v1/videos?query=elephant&category=classroom").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.toString()))))
    }

    @Test
    fun `can find videos by tags`() {
        saveVideo(videoId = 1, tags = listOf("news", "classroom"), title = "ben poos elephants")
        saveVideo(videoId = 2, tags = listOf("classroom"), title = "Video about elephants")

        mockMvc.perform(get("/v1/videos?query=elephants&category=news&category=classroom").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
                .andExpect(jsonPath("$._embedded.videos[*].id", hasItem("1")))
                .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem("2"))))
    }

    @Test
    fun `returns Youtube videos when query matches`() {
        mockMvc.perform(get("/v1/videos?query=jobs").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo("124")))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("elephants took out jobs")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("it's a asset from youtube")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2017-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].contentPartner", equalTo("cp2")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
                .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("YOUTUBE")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT56S")))
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
    fun `returns results when searching by id`() {
        mockMvc.perform(get("/v1/videos?query=id:123,-1").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo("123")))

                .andExpect(jsonPath("$.page.size", Matchers.equalTo(100)))
                .andExpect(jsonPath("$.page.totalElements", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.page.totalPages", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.page.number", Matchers.equalTo(0)))
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
                .andExpect(jsonPath("$.contentPartner", equalTo("cp")))
                .andExpect(jsonPath("$.contentPartnerVideoId", equalTo("content-partner-video-id-123")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
                .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/mpegdash/asset-entry-123.mp4")))
                .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-123.mp4")))
                .andExpect(jsonPath("$.type.id", equalTo(3)))
                .andExpect(jsonPath("$.type.name", equalTo("Instructional Clips")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/123")))
    }

    @Test
    fun `returns 404 for nonexistent video`() {
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
                "playbackProvider": "KALTURA",
                "subjects": ["Maths"]
            }
        """.trimIndent()

        val createdResourceUrl = mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated)
                .andReturn().response.getHeader("Location")

        mockMvc.perform(get(createdResourceUrl!!).asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title", equalTo("AP title")))
                .andExpect(jsonPath("$.subjects", equalTo(listOf("Maths"))))
    }

    @Test
    fun `returns 400 when creating a video without an existing playback`() {
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

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest)
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

    @Test
    fun `subject classifier can add subjects to an existing video with no subjects`() {
        val mathsPatch = """{ "subjects": ["Maths", "Physics"] }"""

        mockMvc.perform(post("/v1/videos/123").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch))
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/123").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.subjects", equalTo(listOf("Maths", "Physics"))))
                .andExpect(jsonPath("$.title", equalTo("powerful asset about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
    }

    @Test
    fun `subject classifier can replace subjects`() {
        mockMvc.perform(post("/v1/videos/123").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content("""{ "subjects": ["Maths"] }"""))
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(post("/v1/videos/123").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content("""{ "subjects": ["Physics"] }"""))
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/123").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.subjects", equalTo(listOf("Physics"))))
    }

    @Test
    fun `other roles are not authorised to add data to a video`() {
        mockMvc.perform(post("/v1/videos/99999").asIngestor()
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden)
    }

    @Test
    fun `it's an error to add data to a nonexistent video with a well-formed ID`() {
        val mathsPatch = """{ "subjects": ["Maths"] }"""

        mockMvc.perform(post("/v1/videos/99999").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `it's an error to add data for a malformed video ID`() {
        val mathsPatch = """{ "subjects": ["Maths"] }"""

        mockMvc.perform(post("/v1/videos/not-a-string").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch))
                .andExpect(status().isNotFound)
    }
}

