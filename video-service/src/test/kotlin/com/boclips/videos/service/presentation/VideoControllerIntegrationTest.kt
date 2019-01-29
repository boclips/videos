package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
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

    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
                playbackId = PlaybackId(value = "ref-id-123", type = PlaybackProviderType.KALTURA),
                title = "powerful asset about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = Duration.ofSeconds(23),
                contentProvider = "cp"
        ).value

        youtubeVideoId = saveVideo(
                playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
                title = "elephants took out jobs",
                description = "it's a asset from youtube",
                date = "2017-02-11",
                duration = Duration.ofSeconds(56),
                contentProvider = "cp2"
        ).value
    }

    @Test
    fun `returns Kaltura videos when query matches`() {
        mockMvc.perform(get("/v1/videos?query=powerful").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful asset about elephants")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].contentPartner", equalTo("cp")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
                .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT23S")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.streamUrl", equalTo("https://stream/applehttp/asset-entry-ref-id-123.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-ref-id-123.mp4")))
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/$kalturaVideoId")))
                .andExpect(jsonPath("$._embedded.videos[0].badges", equalTo(listOf("ad-free"))))

                .andExpect(jsonPath("$.page.size", Matchers.equalTo(100)))
                .andExpect(jsonPath("$.page.totalElements", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.page.totalPages", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.page.number", Matchers.equalTo(0)))
                .andExpect(jsonPath("$._links.prev").doesNotExist())
                .andExpect(jsonPath("$._links.next").doesNotExist())
    }

    @Test
    fun `filters out non classroom results when filter param set`() {
        val excludedVideoId = saveVideo(title = "Non educational video about elephants", typeId = LegacyVideoType.STOCK.id)

        mockMvc.perform(get("/v1/videos?query=elephant&include_tag=classroom").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.value))))
    }

    @Test
    fun `can exclude results for a particular tag`() {
        val excludedVideoId = saveVideo(title = "Elephant news", typeId = LegacyVideoType.NEWS.id)

        mockMvc.perform(get("/v1/videos?query=elephant&exclude_tag=news").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.value))))
    }

    @Test
    fun `can find videos by tags`() {
        val newsAndClassroomVideoId = saveVideo(title = "ben poos elephants", typeId = LegacyVideoType.NEWS.id)
        val classroomVideoId = saveVideo(title = "Video about elephants", typeId = LegacyVideoType.INSTRUCTIONAL_CLIPS.id)

        mockMvc.perform(get("/v1/videos?query=elephants&include_tag=news&include_tag=classroom").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
                .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(newsAndClassroomVideoId.value)))
                .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(classroomVideoId.value))))
    }

    @Test
    fun `returns Youtube videos when query matches`() {
        mockMvc.perform(get("/v1/videos?query=jobs").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("elephants took out jobs")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("it's a asset from youtube")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2017-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].contentPartner", equalTo("cp2")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
                .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("YOUTUBE")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT56S")))
                .andExpect(jsonPath("$._embedded.videos[0].playback.thumbnailUrl", equalTo("https://youtube.com/thumb/yt-id-124.png")))
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/$youtubeVideoId")))
                .andExpect(jsonPath("$._embedded.videos[0].badges", equalTo(listOf("youtube"))))
    }

    @Test
    fun `returns empty videos array when nothing matches`() {
        mockMvc.perform(get("/v1/videos?query=whatdohorseseat").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(0)))
    }

    @Test
    fun `returns results when searching by id`() {
        mockMvc.perform(get("/v1/videos?query=id:$kalturaVideoId,-1").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))

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
        mockMvc.perform(get("/v1/videos/${kalturaVideoId}").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$.title", equalTo("powerful asset about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.contentPartner", equalTo("cp")))
                .andExpect(jsonPath("$.contentPartnerVideoId", equalTo("content-partner-video-id-ref-id-123")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
                .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/applehttp/asset-entry-ref-id-123.mp4")))
                .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-ref-id-123.mp4")))
                .andExpect(jsonPath("$.type.id", equalTo(3)))
                .andExpect(jsonPath("$.type.name", equalTo("Instructional Clips")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))
    }

    @Test
    fun `returns 404 for nonexistent video`() {
        mockMvc.perform(get("/v1/videos/9999").asTeacher())
                .andExpect(status().`is`(404))
    }

    @Test
    fun `returns 200 when video is deleted`() {
        val videoId = saveVideo().value

        mockMvc.perform(delete("/v1/videos/$videoId").asOperator())
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
                "videoType": "INSTRUCTIONAL_CLIPS",
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
    fun `returns a helpful error message when request is not valid`() {
        val content = """
            {
                "provider": "AP"
            }
        """.trimIndent()

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error", containsString("cannot be null")))
    }

    @Test
    fun `returns a CONFLICT and a helpful error message when video already exists`() {
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
                "videoType": "INSTRUCTIONAL_CLIPS",
                "playbackId": "abc1",
                "playbackProvider": "KALTURA",
                "subjects": ["Maths"]
            }
        """.trimIndent()

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated)

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error", containsString("""video from provider "AP" and provider id "1" already exists""")))
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
                "videoType": "INSTRUCTIONAL_CLIPS",
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
    fun `add subjects to an existing video with no subjects`() {
        val mathsPatch = """{ "subjects": ["Maths", "Physics"] }"""

        val videoId = saveVideo(subjects = emptySet())

        mockMvc.perform(post("/v1/videos/${videoId.value}").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch))
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/${videoId.value}").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.subjects", equalTo(listOf("Maths", "Physics"))))
    }

    @Test
    fun `replace subjects`() {
        val videoId = saveVideo(subjects = setOf("Maths")).value

        mockMvc.perform(post("/v1/videos/$videoId").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content("""{ "subjects": ["Physics"] }"""))
                .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
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

