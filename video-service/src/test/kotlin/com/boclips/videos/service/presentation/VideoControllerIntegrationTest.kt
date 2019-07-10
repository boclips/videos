package com.boclips.videos.service.presentation

import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import com.boclips.videos.service.application.video.BulkVideoSearchUpdate
import com.boclips.videos.service.domain.model.common.BoundedAgeRange
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoRepository.Companion.collectionName
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.LocalDate

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var bulkVideoSearchUpdate: BulkVideoSearchUpdate

    lateinit var disabledVideoId: String
    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-123", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            contentProvider = "cp",
            legalRestrictions = "None",
            ageRange = BoundedAgeRange(min = 5, max = 7)
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended overweight people",
            date = "2018-05-10",
            duration = Duration.ofSeconds(6),
            contentProvider = "cp",
            ageRange = UnboundedAgeRange
        ).value


        hideFromAllDeliveryMethods(disabledVideoId)

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofSeconds(56),
            contentProvider = "cp2",
            ageRange = UnboundedAgeRange
        ).value
    }


    @Test
    fun `returns Kaltura videos when query matches`() {
        setVideoSubjects(kalturaVideoId, saveSubject("Maths"))

        mockMvc.perform(get("/v1/videos?query=powerful").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful video about elephants")))
            .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
            .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
            .andExpect(jsonPath("$._embedded.videos[0].contentPartner", equalTo("cp")))
            .andExpect(jsonPath("$._embedded.videos[0].legalRestrictions", equalTo("None")))
            .andExpect(jsonPath("$._embedded.videos[0].subjects[0]", equalTo("Maths")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
            .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT23S")))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback.streamUrl",
                    equalTo("https://stream/applehttp/video-entry-ref-id-123.mp4")
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

            .andExpect(jsonPath("$.page.size", equalTo(100)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(1)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(1)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))
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
            .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("it's a video from youtube")))
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

            .andExpect(jsonPath("$.page.size", equalTo(100)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(1)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(1)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))
    }

    @Test
    fun `returns video within specified duration`() {
        mockMvc.perform(get("/v1/videos?query=powerful&duration_min=PT20S&duration_max=PT24S").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `returns video with correct source`() {
        mockMvc.perform(get("/v1/videos?query=elephants&source=boclips").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `returns video within specified age range`() {
        mockMvc.perform(get("/v1/videos?query=elephants&age_range_min=5&age_range_max=11").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `returns 400 with invalid source`() {
        mockMvc.perform(get("/v1/videos?query=elephants&source=invalidoops").asTeacher())
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 400 with invalid duration`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration_min=invalidoops").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns video within specified released data`() {
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_from=2018-01-11&released_date_to=2018-03-11").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `returns 400 with invalid date filter`() {
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_from=invalidoops").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_to=invalidoops").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns video matching query and subject`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            contentProvider = "cp",
            legalRestrictions = "None"
        ).value

        val subjectId = saveSubject("Maths")
        setVideoSubjects(videoId, subjectId)

        mockMvc.perform(get("/v1/videos?query=elephants&subject=${subjectId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoId)))
    }

    @Test
    fun `returns videos of given subject`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            contentProvider = "cp",
            legalRestrictions = "None"
        ).value

        val subjectId = saveSubject("Maths")
        setVideoSubjects(videoId, subjectId)

        mockMvc.perform(get("/v1/videos?subject=${subjectId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoId)))
    }

    @Test
    fun `returns videos of given subjects`() {
        saveVideo()
        val mathsVideoId = saveVideo().value
        val englishVideoId = saveVideo().value

        val mathsId = saveSubject("Maths")
        val englishId = saveSubject("English")
        setVideoSubjects(mathsVideoId, mathsId)
        setVideoSubjects(englishVideoId, englishId)

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value}&subject=${englishId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(mathsVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(englishVideoId)))
    }

    @Test
    fun `returns videos of given subjects with comma syntax`() {
        saveVideo()
        val mathsVideoId = saveVideo().value
        val englishVideoId = saveVideo().value

        val mathsId = saveSubject("Maths")
        val englishId = saveSubject("English")
        setVideoSubjects(mathsVideoId, mathsId)
        setVideoSubjects(englishVideoId, englishId)

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value},${englishId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(mathsVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(englishVideoId)))
    }

    @Test
    fun `returns 200 for valid video as boclips employee`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
            .andExpect(jsonPath("$.description", equalTo("test description 3")))
            .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
            .andExpect(jsonPath("$.contentPartner", equalTo("cp")))
            .andExpect(jsonPath("$.contentPartnerVideoId", equalTo("content-partner-video-id-ref-id-123")))
            .andExpect(jsonPath("$.playback.id").exists())
            .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
            .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
            .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/applehttp/video-entry-ref-id-123.mp4")))
            .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-ref-id-123.mp4")))
            .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
            .andExpect(jsonPath("$.type.id", equalTo(3)))
            .andExpect(jsonPath("$.type.name", equalTo("Instructional Clips")))
            .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))
            .andExpect(jsonPath("$.ageRange.min", equalTo(5)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(7)))
    }

    @Test
    fun `returns 200 for valid video as anonymous user`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
            .andExpect(jsonPath("$.description", equalTo("test description 3")))
            .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
            .andExpect(jsonPath("$.contentPartner", equalTo("cp")))
            .andExpect(jsonPath("$.playback.id").exists())
            .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
            .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
            .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/applehttp/video-entry-ref-id-123.mp4")))
            .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-ref-id-123.mp4")))
            .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
            .andExpect(jsonPath("$.ageRange.min", equalTo(5)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(7)))
            .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))

            .andExpect(jsonPath("$.contentPartnerVideoId").doesNotExist())
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.status").doesNotExist())
    }

    @Test
    fun `returns 200 for valid video as API user`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId").asApiUser())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
            .andExpect(jsonPath("$.description", equalTo("test description 3")))
            .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
            .andExpect(jsonPath("$.contentPartner", equalTo("cp")))
            .andExpect(jsonPath("$.playback.id").exists())
            .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
            .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
            .andExpect(jsonPath("$.playback.streamUrl", equalTo("https://stream/applehttp/video-entry-ref-id-123.mp4")))
            .andExpect(jsonPath("$.playback.thumbnailUrl", equalTo("https://thumbnail/thumbnail-entry-ref-id-123.mp4")))
            .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
            .andExpect(jsonPath("$.ageRange.min", equalTo(5)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(7)))
            .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))

            .andExpect(jsonPath("$.contentPartnerVideoId").doesNotExist())
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.status").doesNotExist())
    }

    @Test
    fun `transcript link is not present when not authenticated`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.transcript").doesNotExist())
    }

    @Test
    fun `transcript link is present when authenticated`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.transcript.href").exists())
    }

    @Test
    fun `returns 200 for valid video alias`() {
        val title = "Back to the Future II"
        val alias = "123123"
        val videoId = saveVideo(title = title)

        mongoVideosCollection().findOneAndUpdate(
            eq("title", title),
            set("aliases", alias)
        )

        mockMvc.perform(get("/v1/videos/$alias").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId.value)))
    }

    @Test
    fun `returns 404 for nonexistent video`() {
        mockMvc.perform(get("/v1/videos/9999").asTeacher())
            .andExpect(status().`is`(404))
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 404 for non hex id`() {
        mockMvc.perform(get("/v1/videos/not-quite-hexadecimal").asTeacher())
            .andExpect(status().`is`(404))
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 200 when video is deleted`() {
        val videoId = saveVideo().value

        mockMvc.perform(delete("/v1/videos/$videoId").asOperator())
            .andExpect(status().`is`(200))
    }

    @Test
    fun `rates video`() {
        val videoId = saveVideo().value

        mockMvc.perform(patch("/v1/videos/$videoId?rating=3").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$.rating", equalTo(3)))

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$.rating", equalTo(3)))
    }

    @Test
    fun `invalid rating`() {
        val videoId = saveVideo().value

        mockMvc.perform(patch("/v1/videos/$videoId?rating=").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
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
                "playbackProvider": "KALTURA"
            }
        """.trimIndent()

        val createdResourceUrl =
            mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated)
                .andReturn().response.getHeader("Location")

        mockMvc.perform(get(createdResourceUrl!!).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title", equalTo("AP title")))
    }

    @Test
    fun `create new video with subjects`() {
        val subjectId = saveSubject("Maths")

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
                "subjects": ["${subjectId.value}"]
            }
        """.trimIndent()

        val createdResourceUrl =
            mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated)
                .andReturn().response.getHeader("Location")

        mockMvc.perform(get(createdResourceUrl!!).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subjects[0]", equalTo("Maths")))
    }

    @Test
    fun `returns a helpful error message when request is not valid`() {
        val content = """
            {
                "provider": "AP",
                "providerVideoId": "1"
            }
        """.trimIndent()

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("Illegal playback id")))
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
                "playbackProvider": "KALTURA"
            }
        """.trimIndent()

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isCreated)

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isConflict)
            .andExpect(
                jsonPath(
                    "$.message",
                    containsString("""video from provider "AP" and provider id "1" already exists""")
                )
            )
            .andExpectApiErrorPayload()
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
            .andExpectApiErrorPayload()
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
            .andExpectApiErrorPayload()
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
    fun `returns all enabled and disabled video by ID`() {
        mockMvc.perform(
            post("/v1/videos/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids": ["$disabledVideoId", "$kalturaVideoId", "$youtubeVideoId"]}""").asBoclipsEmployee()
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(3)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(disabledVideoId)))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("elephants eat a lot")))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].hiddenFromSearchForDeliveryMethods",
                    contains("STREAM", "DOWNLOAD")
                )
            )
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
    fun `dedupe videos searching by IDs`() {
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
    fun `bulk updates to search blacklist take effect`() {
        val videoIds = listOf(saveVideo().value, saveVideo().value, saveVideo().value)

        mockMvc.perform(
            patch("/v1/videos").asBoclipsEmployee()
                .content("""{ "ids": ["${videoIds[0]}", "${videoIds[1]}"], "hiddenFromSearchForDeliveryMethods": ["DOWNLOAD"] }""")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        videoIds.zip(
            listOf(
                Pair("SEARCHABLE", listOf("DOWNLOAD")),
                Pair("SEARCHABLE", listOf("DOWNLOAD")),
                Pair("SEARCHABLE", emptyList())
            )
        ).forEach { (id, statuses) ->
            mockMvc.perform(get("/v1/videos/$id").asBoclipsEmployee())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.hiddenFromSearchForDeliveryMethods", equalTo(statuses.second)))
        }
    }

    @Test
    fun `it sorts news by releaseDate descending`() {
        val today = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            legacyType = LegacyVideoType.NEWS
        ).value
        val yesterday = saveVideo(
            title = "Yesterday Video",
            date = LocalDate.now().minusDays(1).toString(),
            legacyType = LegacyVideoType.NEWS
        ).value
        val tomorrow = saveVideo(
            title = "Tomorrow Video",
            date = LocalDate.now().plusDays(1).toString(),
            legacyType = LegacyVideoType.NEWS
        ).value

        val resultActions = mockMvc.perform(
            get("/v1/videos?query=video&sort_by=RELEASE_DATE")
                .contentType(MediaType.APPLICATION_JSON).asBoclipsEmployee()
        )

        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(tomorrow)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(today)))
            .andExpect(jsonPath("$._embedded.videos[2].id", equalTo(yesterday)))
    }

    @Test
    fun `going to the transcripts endpoint for a video with transcripts returns the transcripts`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(equalTo("Some content in the video")))
            .andExpect(header().string("Content-Disposition", equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `going to the transcripts endpoint for a video without transcripts returns 404`() {
        val videoId = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            legacyType = LegacyVideoType.NEWS
        ).value

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isNotFound)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `it returns a transcript uri when there is a transcript to download`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$._links.transcript.href", containsString("/videos/$videoId/transcript")))
    }

    @Test
    fun `it does not return a transcript uri when there is no transcript`() {
        val videoId = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            legacyType = LegacyVideoType.NEWS
        ).value

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$._links.transcript.href").doesNotHaveJsonPath())
    }

    private fun saveVideoWithTranscript(): String {
        val videoId = saveVideo(
            title = "Today Video?",
            date = LocalDate.now().toString(),
            legacyType = LegacyVideoType.NEWS
        ).value

        assertNotNull(
            mongoVideosCollection().findOneAndUpdate(
                eq("title", "Today Video?"),
                set("transcript", "Some content in the video")
            )
        )
        return videoId
    }


    private fun hideFromAllDeliveryMethods(videoId: String) {
        bulkUpdateVideo.invoke(
            bulkUpdateRequest = BulkUpdateRequest(
                ids = listOf(videoId),
                hiddenFromSearchForDeliveryMethods = setOf(
                    DeliveryMethodResource.STREAM,
                    DeliveryMethodResource.DOWNLOAD
                )
            )
        )

        bulkVideoSearchUpdate.invoke(
            event = VideosExclusionFromDownloadRequested.builder().videoIds(
                listOf(
                    videoId
                )
            ).build()
        )
        bulkVideoSearchUpdate.invoke(
            event = VideosExclusionFromStreamRequested.builder().videoIds(
                listOf(
                    videoId
                )
            ).build()
        )
    }

    private fun mongoVideosCollection() = mongoClient.getDatabase(DATABASE_NAME).getCollection(collectionName)
}

