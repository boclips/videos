package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository.Companion.collectionName
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.LocalDate

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var disabledVideoId: String
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
            contentProvider = "cp",
            legalRestrictions = "None"
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended overweight people",
            date = "2018-05-10",
            duration = Duration.ofSeconds(6),
            contentProvider = "cp"
        ).value
        changeVideoStatus(disabledVideoId, VideoResourceStatus.SEARCH_DISABLED)

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
            .andExpect(jsonPath("$._embedded.videos[0].legalRestrictions", equalTo("None")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
            .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT23S")))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback.streamUrl",
                    equalTo("https://stream/applehttp/asset-entry-ref-id-123.mp4")
                )
            )
            .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("STREAM")))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback.thumbnailUrl",
                    equalTo("https://thumbnail/thumbnail-entry-ref-id-123.mp4")
                )
            )
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
        val excludedVideoId =
            saveVideo(title = "Non educational video about elephants", legacyType = LegacyVideoType.STOCK)

        mockMvc.perform(get("/v1/videos?query=elephant&include_tag=classroom").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.value))))
    }

    @Test
    fun `can exclude results for a particular tag`() {
        val excludedVideoId = saveVideo(title = "Elephant news", legacyType = LegacyVideoType.NEWS)

        mockMvc.perform(get("/v1/videos?query=elephant&exclude_tag=news").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.value))))
    }

    @Test
    fun `can find videos by tags`() {
        val newsAndClassroomVideoId = saveVideo(title = "ben poos elephants", legacyType = LegacyVideoType.NEWS)
        val classroomVideoId =
            saveVideo(title = "Video about elephants", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)

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
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback.thumbnailUrl",
                    equalTo("https://youtube.com/thumb/yt-id-124.png")
                )
            )
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
            .andExpect(jsonPath("$.status", equalTo("SEARCHABLE")))
            .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))
    }

    @Test
    fun `returns 200 for valid video alias`() {
        val title = "Back to the Future II"
        val alias = "123123"
        val assetId = saveVideo(title = title)

        mongoVideosCollection().findOneAndUpdate(
            eq("title", title),
            set("aliases", alias)
        )

        mockMvc.perform(get("/v1/videos/$alias").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(assetId.value)))
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
    fun `create new video`() {
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                id = "entry-$123",
                referenceId = "abc1",
                duration = Duration.ofMinutes(1)
            )
        )

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

        val createdResourceUrl =
            mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
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
            .andExpect(jsonPath("$.error", containsString("not found")))
    }

    @Test
    fun `returns a CONFLICT and a helpful error message when video already exists`() {
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                id = "entry-$123",
                referenceId = "abc1",
                duration = Duration.ofMinutes(1)
            )
        )

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
            .andExpect(
                jsonPath(
                    "$.error",
                    containsString("""video from provider "AP" and provider id "1" already exists""")
                )
            )
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

        mockMvc.perform(
            post("/v1/videos/${videoId.value}").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch)
        )
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/${videoId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subjects", equalTo(listOf("Maths", "Physics"))))
    }

    @Test
    fun `replace subjects`() {
        val videoId = saveVideo(subjects = setOf("Maths")).value

        mockMvc.perform(
            post("/v1/videos/$videoId").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content("""{ "subjects": ["Physics"] }""")
        )
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subjects", equalTo(listOf("Physics"))))
    }

    @Test
    fun `setting subject doesn't destroy the alias`() {
        val title = "Back to the Future II"
        val alias = "123123"
        saveVideo(title = title)

        mongoVideosCollection().findOneAndUpdate(
            eq("title", title),
            set("aliases", alias)
        )

        mockMvc.perform(
            post("/v1/videos/$alias").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content("""{ "subjects": ["Physics"] }""")
        )
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(get("/v1/videos/$alias").asTeacher())
            .andExpect(status().isOk)
    }

    @Test
    fun `other roles are not authorised to add data to a video`() {
        mockMvc.perform(
            post("/v1/videos/99999").asIngestor()
                .contentType(MediaType.APPLICATION_JSON).content("{}")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `it's an error to add data to a nonexistent video with a well-formed ID`() {
        val mathsPatch = """{ "subjects": ["Maths"] }"""

        mockMvc.perform(
            post("/v1/videos/${TestFactories.aValidId()}").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `it's an error to add data for a malformed video ID`() {
        val mathsPatch = """{ "subjects": ["Maths"] }"""

        mockMvc.perform(
            post("/v1/videos/not-a-string").asSubjectClassifier()
                .contentType(MediaType.APPLICATION_JSON).content(mathsPatch)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `returns all enabled and disabled video by ID`() {
        mockMvc.perform(
            post("/v1/videos/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids": ["$disabledVideoId", "$kalturaVideoId", "$youtubeVideoId"]}""").asBoclipsEmployee()
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(3)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(disabledVideoId)))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("elephants eat a lot")))
            .andExpect(jsonPath("$._embedded.videos[0].status", equalTo("SEARCH_DISABLED")))
            .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/$disabledVideoId")))

            .andExpect(jsonPath("$.page").doesNotExist())
    }

    @Test
    fun `ignores unknown videos searching by IDs`() {
        mockMvc.perform(
            post("/v1/videos/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids": ["nonsense", "$disabledVideoId", "nonsense"]}""").asBoclipsEmployee()
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(disabledVideoId)))
    }

    @Test
    fun `dedup videos searching by IDs`() {
        mockMvc.perform(
            post("/v1/videos/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids": ["$disabledVideoId", "$disabledVideoId"]}""").asBoclipsEmployee()
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(disabledVideoId)))
    }

    @Test
    fun `returns 201 when id searching by alias`() {
        val title = "Back to the Future II"
        val alias = "123123"
        saveVideo(title = title)

        mongoVideosCollection().findOneAndUpdate(
            eq("title", title),
            set("aliases", alias)
        )

        mockMvc.perform(
            post("/v1/videos/search").contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids": ["$alias"]}""").asBoclipsEmployee()
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `it allows videos to be updated in bulk`() {
        val videoIds = listOf(
            saveVideo(searchable = true).value,
            saveVideo(searchable = true).value,
            saveVideo(searchable = true).value
        )

        mockMvc.perform(
            patch("/v1/videos").asBoclipsEmployee()
                .content("""{ "ids": ["${videoIds[0]}", "${videoIds[1]}"], "status": "SEARCH_DISABLED" }""")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        videoIds.zip(listOf("SEARCH_DISABLED", "SEARCH_DISABLED", "SEARCHABLE")).forEach { (id, status) ->
            mockMvc.perform(get("/v1/videos/$id").asBoclipsEmployee())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status", equalTo(status)))
        }
    }

    @Test
    fun `it sorts news by releaseDate descending`() {
        val today = saveVideo(
            title = "Today Video",
            searchable = true,
            date = LocalDate.now().toString(),
            legacyType = LegacyVideoType.NEWS
        ).value
        val yesterday = saveVideo(
            title = "Yesterday Video",
            searchable = true,
            date = LocalDate.now().minusDays(1).toString(),
            legacyType = LegacyVideoType.NEWS
        ).value
        val tomorrow = saveVideo(
            title = "Tomorrow Video",
            searchable = true,
            date = LocalDate.now().plusDays(1).toString(),
            legacyType = LegacyVideoType.NEWS
        ).value

        val resultActions = mockMvc.perform(
            get("/v1/videos?query=video&sort_by=RELEASE_DATE")
                .contentType(MediaType.APPLICATION_JSON).asBoclipsEmployee()
        )

        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(3)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(tomorrow)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(today)))
            .andExpect(jsonPath("$._embedded.videos[2].id", equalTo(yesterday)))

            .andExpect(jsonPath("$.page.totalElements", Matchers.equalTo(3)))
            .andExpect(jsonPath("$.page.totalPages", Matchers.equalTo(1)))
    }

    private fun mongoVideosCollection() = mongoClient.getDatabase(DATABASE_NAME).getCollection(collectionName)
}

