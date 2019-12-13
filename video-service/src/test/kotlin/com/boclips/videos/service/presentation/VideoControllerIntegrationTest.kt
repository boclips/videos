package com.boclips.videos.service.presentation

import com.boclips.eventbus.events.video.VideosUpdated
import com.boclips.users.client.model.Subject
import com.boclips.users.client.model.TeacherPlatformAttributes
import com.boclips.users.client.model.User
import com.boclips.videos.service.domain.model.common.BoundedAgeRange
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository.Companion.collectionName
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import com.damnhandy.uri.template.UriTemplate
import com.jayway.jsonpath.JsonPath
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import javax.servlet.http.Cookie

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var disabledVideoId: String
    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            contentProvider = "cp",
            legalRestrictions = "None",
            ageRange = BoundedAgeRange(min = 5, max = 7)
        ).value

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofSeconds(56),
            contentProvider = "cp2",
            ageRange = UnboundedAgeRange
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended Jose Carlos Valero Sanchez",
            date = "2018-05-10",
            duration = Duration.ofSeconds(6),
            contentProvider = "cp",
            ageRange = UnboundedAgeRange
        ).value

        removeDistributionMethods(disabledVideoId)
    }

    @Test
    fun `search videos as teacher`() {
        setVideoSubjects(kalturaVideoId, saveSubject("Maths").id)

        mockMvc.perform(get("/v1/videos?query=powerful").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful video about elephants")))
            .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
            .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
            .andExpect(jsonPath("$._embedded.videos[0].contentPartner", equalTo("cp")))
            .andExpect(jsonPath("$._embedded.videos[0].legalRestrictions", equalTo("None")))
            .andExpect(jsonPath("$._embedded.videos[0].subjects[0].id").exists())
            .andExpect(jsonPath("$._embedded.videos[0].subjects[0].name", equalTo("Maths")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
            .andExpect(jsonPath("$._embedded.videos[0].playback.referenceId", equalTo("ref-entry-id-123")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT23S")))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback.streamUrl",
                    equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                )
            )
            .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("STREAM")))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback.thumbnailUrl",
                    equalTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/entry-id-123/width/500/vid_slices/3/vid_slice/1")
                )
            )
            .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/$kalturaVideoId")))
            .andExpect(jsonPath("$._embedded.videos[0].badges", equalTo(listOf("ad-free"))))

            .andExpect(jsonPath("$.page.size", equalTo(100)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(1)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(1)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))
    }

    @Test
    fun `can filter by content type`() {
        val stockVideoId = saveVideo(title = "content type filtering", type = ContentType.STOCK)
        saveVideo(title = "content type filtering", type = ContentType.NEWS)

        mockMvc.perform(get("/v1/videos?query=content&type=STOCK").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(stockVideoId.value)))
    }

    @Test
    fun `filters out non classroom results when filter param set`() {
        val excludedVideoId =
            saveVideo(title = "Non educational video about elephants", type = ContentType.STOCK)

        mockMvc.perform(get("/v1/videos?query=elephant&include_tag=classroom").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.value))))
    }

    @Test
    fun `can exclude results for a particular tag`() {
        val excludedVideoId = saveVideo(title = "Elephant news", type = ContentType.NEWS)

        mockMvc.perform(get("/v1/videos?query=elephant&exclude_tag=news").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(excludedVideoId.value))))
    }

    @Test
    fun `can find videos by tags`() {
        val newsAndClassroomVideoId = saveVideo(title = "ben poos elephants", type = ContentType.NEWS)
        val classroomVideoId =
            saveVideo(title = "Video about elephants", type = ContentType.INSTRUCTIONAL_CLIPS)

        mockMvc.perform(get("/v1/videos?query=elephants&include_tag=news&include_tag=classroom").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(newsAndClassroomVideoId.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(classroomVideoId.value))))
    }

    @Test
    fun `can find videos by promoted flag`() {
        val promotedVideoId = saveVideo(title = "ben poos elephants")
        val unpromotedVideoId = saveVideo(title = "Video about elephants")

        setPromoted(promotedVideoId.value, true)
        setPromoted(unpromotedVideoId.value, false)

        mockMvc.perform(get("/v1/videos?promoted=true").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(promotedVideoId.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(unpromotedVideoId.value))))
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
            .andExpect(jsonPath("$._embedded.videos[0].playback.downloadUrl").doesNotExist())
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback._links.thumbnail.href",
                    equalTo("https://youtube.com/thumb/yt-id-124.png")
                )
            )
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
    fun `returns video with specific content partner`() {
        mockMvc.perform(get("/v1/videos?content_partner=cp2").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `returns videos with specific list of content partners`() {
        val newVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            contentProvider = "cp3",
            legalRestrictions = "None"
        ).value

        mockMvc.perform(get("/v1/videos?content_partner=cp2&content_partner=cp3").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(newVideoId)))
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

        val subjectId = saveSubject("Maths").id
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

        val subjectId = saveSubject("Maths").id
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

        val mathsId = saveSubject("Maths").id
        val englishId = saveSubject("English").id
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

        val mathsId = saveSubject("Maths").id
        val englishId = saveSubject("English").id
        setVideoSubjects(mathsVideoId, mathsId)
        setVideoSubjects(englishVideoId, englishId)

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value},${englishId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(mathsVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(englishVideoId)))
    }

    @Test
    fun `returns a hls stream link`() {
        val videoId = saveVideo(playbackId = PlaybackId(PlaybackProviderType.KALTURA, "entry-id-123"))

        mockMvc.perform(get("/v1/videos/${videoId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId.value)))
            .andExpect(
                jsonPath(
                    "$.playback._links.hlsStream.href",
                    equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                )
            )
    }

    @Nested
    inner class VideoResourceProjections {
        @Test
        fun `returns 200 for valid video as boclips employee`() {
            mockMvc.perform(get("/v1/videos/$kalturaVideoId").asBoclipsEmployee())
                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
                .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.createdBy", equalTo("cp")))
                .andExpect(jsonPath("$.contentPartner", equalTo("cp")))
                .andExpect(jsonPath("$.contentPartnerId").exists())
                .andExpect(jsonPath("$.contentPartnerVideoId", equalTo("content-partner-video-id-entry-id-123")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").exists())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
                .andExpect(
                    jsonPath(
                        "$.playback.streamUrl",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.playback.thumbnailUrl",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/entry-id-123/width/500/vid_slices/3/vid_slice/1")
                    )
                )
                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").isNotEmpty())
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.videoPreview.href",
                        containsString("/vid_slices/{thumbnailCount}")
                    )
                )
                .andExpect(jsonPath("$.playback._links.videoPreview.templated", equalTo(true)))
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
                .andExpect(jsonPath("$.createdBy", equalTo("cp")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").doesNotExist())
                .andExpect(jsonPath("$.playback.downloadUrl").doesNotExist())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
                .andExpect(
                    jsonPath(
                        "$.playback.streamUrl",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.playback.thumbnailUrl",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/entry-id-123/width/500/vid_slices/3/vid_slice/1")
                    )
                )
                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").doesNotExist())
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.videoPreview.href",
                        containsString("/vid_slices/{thumbnailCount}")
                    )
                )
                .andExpect(jsonPath("$.playback._links.videoPreview.templated", equalTo(true)))
                .andExpect(jsonPath("$.ageRange.min", equalTo(5)))
                .andExpect(jsonPath("$.ageRange.max", equalTo(7)))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))
                .andExpect(
                    jsonPath(
                        "$._links.${VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION}.href",
                        containsString("/videos/$kalturaVideoId")
                    )
                )
                .andExpect(jsonPath("$._links.share.href").doesNotExist())
                .andExpect(jsonPath("$._links.validateShareCode.href", containsString("/videos/$kalturaVideoId/match?shareCode={shareCode}")))
                .andExpect(jsonPath("$.contentPartnerVideoId").doesNotExist())
                .andExpect(jsonPath("$.contentPartnerId").doesNotExist())
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
                .andExpect(jsonPath("$.createdBy", equalTo("cp")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").doesNotExist())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT23S")))
                .andExpect(
                    jsonPath(
                        "$.playback.streamUrl",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.playback.thumbnailUrl",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/entry-id-123/width/500/vid_slices/3/vid_slice/1")
                    )
                )
                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").doesNotExist())
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.videoPreview.href",
                        containsString("/vid_slices/{thumbnailCount}")
                    )
                )
                .andExpect(jsonPath("$.playback._links.videoPreview.templated", equalTo(true)))
                .andExpect(jsonPath("$.ageRange.min", equalTo(5)))
                .andExpect(jsonPath("$.ageRange.max", equalTo(7)))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))

                .andExpect(jsonPath("$.contentPartnerVideoId").doesNotExist())
                .andExpect(jsonPath("$.contentPartnerId").doesNotExist())
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())
        }
    }

    @Test
    fun `returns 200 for valid video as Teacher user`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$._links.share.href", containsString("/videos/$kalturaVideoId?sharing=true")))
    }

    @Test
    fun `sets playback consumer cookie when not already present`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId"))
            .andExpect(status().isOk)
            .andExpect(cookie().exists(Cookies.PLAYBACK_DEVICE))
            .andExpect(cookie().httpOnly(Cookies.PLAYBACK_DEVICE, true))
            .andExpect(cookie().path(Cookies.PLAYBACK_DEVICE, "/"))
            .andExpect(cookie().secure(Cookies.PLAYBACK_DEVICE, true))
            .andExpect(cookie().maxAge(Cookies.PLAYBACK_DEVICE, Duration.ofDays(365).seconds.toInt()))
    }

    @Test
    fun `does not set a playback consumer cookie if already present`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId").cookie(Cookie(Cookies.PLAYBACK_DEVICE, "a-consumer-id")))
            .andExpect(status().isOk)
            .andExpect(cookie().doesNotExist(Cookies.PLAYBACK_DEVICE))
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

        val rateUrl = getRatingLink(videoId)

        mockMvc.perform(patch(rateUrl, 3).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.rating", equalTo(3.0)))
            .andExpect(jsonPath("$.yourRating", equalTo(3.0)))
            .andExpect(jsonPath("$._links.rate.href").exists())

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.rating", equalTo(3.0)))
            .andExpect(jsonPath("$.yourRating", equalTo(3.0)))
            .andExpect(jsonPath("$._links.rate").exists())
    }

    @Test
    fun `accessing a shared video with correct code`() {
        val videoId = saveVideo().value
        val user = userServiceClient.addUser(User("sharer-test@boclips.com", "orgId", emptyList<Subject>(), TeacherPlatformAttributes("abcd")))

        mockMvc.perform(patch("/v1/videos/$videoId?sharing=true").asTeacher("sharer-test@boclips.com"))
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoId/match?shareCode=${user.teacherPlatformAttributes.shareCode}"))
            .andExpect(status().isOk)
    }

    @Test
    fun `accessing a shared video with an incorrect code`() {
        val videoId = saveVideo().value
        userServiceClient.addUser(User("sharer-test@boclips.com", "orgId", emptyList<Subject>(), TeacherPlatformAttributes("abcd")))

        mockMvc.perform(patch("/v1/videos/$videoId?sharing=true").asTeacher("sharer-test@boclips.com"))
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoId/match?shareCode=1234"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `update video metadata`() {
        val videoId = saveVideo(title = "Old title", description = "Old description").value

        val updateLink = getUpdateLink(videoId).expand(
            mapOf(
                "title" to "New title",
                "description" to "New description",
                "promoted" to "true"
            )
        )

        mockMvc.perform(patch(URI.create(updateLink)).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title", equalTo("New title")))
            .andExpect(jsonPath("$.description", equalTo("New description")))
            .andExpect(jsonPath("$.promoted", equalTo(true)))

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title", equalTo("New title")))
            .andExpect(jsonPath("$.description", equalTo("New description")))
            .andExpect(jsonPath("$.promoted", equalTo(true)))
    }

    @Test
    fun `multiple ratings uses average but overrides same user's rating`() {
        val videoId = saveVideo().value
        val rateUrl = getRatingLink(videoId)

        mockMvc.perform(patch(rateUrl, 1).asTeacher("teacher-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.rating", equalTo(1.0)))
            .andExpect(jsonPath("$.yourRating", equalTo(1.0)))

        mockMvc.perform(patch(rateUrl, 3).asTeacher("teacher-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.rating", equalTo(3.0)))
            .andExpect(jsonPath("$.yourRating", equalTo(3.0)))

        mockMvc.perform(patch(rateUrl, 5).asTeacher("teacher-2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.rating", equalTo(4.0)))
            .andExpect(jsonPath("$.yourRating", equalTo(5.0)))
    }

    @Test
    fun `invalid rating`() {
        val videoId = saveVideo().value

        mockMvc.perform(patch("/v1/videos/$videoId?rating=").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `tags video`() {
        val videoId = saveVideo().value

        val tagVideoUrl = getTaggingLink(videoId)
        val tagUrl = createTag("A tag").andReturn().response.getHeader("Location")!!

        mockMvc.perform(
            patch(tagVideoUrl).content(tagUrl)
                .contentType("text/uri-list").asTeacher()
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bestFor.label", equalTo("A tag")))
            .andExpect(jsonPath("$._links.tag").doesNotExist())

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bestFor.label", equalTo("A tag")))
            .andExpect(jsonPath("$._links.tag").doesNotExist())
    }

    @Test
    fun `invalid tagging`() {
        val videoId = saveVideo().value

        val tagUrl = getTaggingLink(videoId)

        mockMvc.perform(
            patch(tagUrl).content("not really a tag")
                .contentType("text/uri-list").asTeacher()
        )
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `create new video`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartnerId = saveContentPartner().contentPartnerId.value

        val content = """
            {
                "providerVideoId": "1",
                "providerId": "$contentPartnerId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoType": "INSTRUCTIONAL_CLIPS",
                "playbackId": "entry-$123",
                "playbackProvider": "KALTURA"
            }
        """.trimIndent()

        val createdResourceUrl =
            mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated)
                .andReturn().response.getHeader("Location")

        mockMvc.perform(get(createdResourceUrl!!).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title", equalTo("AP title")))
            .andExpect(jsonPath("$.playback.id", equalTo("entry-\$123")))
            .andExpect(jsonPath("$.playback.referenceId", equalTo("ref-entry-\$123")))
    }

    @Test
    fun `create new video with subjects`() {
        val subjectId = saveSubject("Maths").id
        val contentPartnerId = saveContentPartner().contentPartnerId.value

        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val content = """
            {
                "providerVideoId": "1",
                "providerId": "$contentPartnerId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoType": "INSTRUCTIONAL_CLIPS",
                "playbackId": "entry-$123",
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
            .andExpect(jsonPath("$.subjects[0].name", equalTo("Maths")))
            .andExpect(jsonPath("$.subjects[0].id").exists())
    }

    @Test
    fun `search videos and sort by rating`() {
        val firstTitle = "low-rated"
        val secondTitle = "high-rated"
        val thirdTitle = "mid-rated"

        val firstVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-1", type = PlaybackProviderType.KALTURA),
            title = firstTitle
        )

        val secondVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-2", type = PlaybackProviderType.YOUTUBE),
            title = secondTitle
        )

        val thirdVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-3", type = PlaybackProviderType.KALTURA),
            title = thirdTitle
        )

        setRating(firstVideoId, 0)
        setRating(secondVideoId, 5)
        setRating(thirdVideoId, 3)

        // first page
        mockMvc.perform(get("/v1/videos?sort_by=RATING&size=2&page=0").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo(secondTitle)))
            .andExpect(jsonPath("$._embedded.videos[1].title", equalTo(thirdTitle)))

        // second page
        mockMvc.perform(get("/v1/videos?sort_by=RATING&size=2&page=1").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo(firstTitle)))
    }

    @Test
    fun `returns a helpful error message when request is not valid`() {
        val contentPartnerId = saveContentPartner().contentPartnerId.value

        val content = """
            {
                "providerVideoId": "1",
                "providerId": "$contentPartnerId"
            }
        """.trimIndent()

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("Illegal playback id")))
    }

    @Test
    fun `returns a CONFLICT and a helpful error message when video already exists`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartnerId = saveContentPartner(name = "AP").contentPartnerId.value

        val content = """
            {
                "providerVideoId": "1",
                "providerId": "$contentPartnerId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoType": "INSTRUCTIONAL_CLIPS",
                "playbackId": "entry-$123",
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
                "providerVideoId": "1",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoType": "INSTRUCTIONAL_CLIPS",
                "playbackId": "this-playback-does-not-exist",
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
                    "$._embedded.videos[0].distributionMethods",
                    hasSize<Int>(0)
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
    fun `bulk updates disables videos through streaming search`() {
        val videoIds = listOf(saveVideo().value, saveVideo().value, saveVideo().value)

        mockMvc.perform(
            patch("/v1/videos").asBoclipsEmployee()
                .content(
                    """{
                    "ids": ["${videoIds[0]}", "${videoIds[1]}"],
                    "distributionMethods": ["${DistributionMethodResource.DOWNLOAD}"] }
                    """.trimMargin()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        videoIds
            .zip(
                listOf(
                    listOf(DistributionMethodResource.DOWNLOAD.toString()),
                    listOf(DistributionMethodResource.DOWNLOAD.toString()),
                    listOf(DistributionMethodResource.DOWNLOAD.toString(), DistributionMethodResource.STREAM.toString())
                )
            ).forEach { (id, statuses) ->
                mockMvc.perform(get("/v1/videos/$id").asBoclipsEmployee())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.distributionMethods", equalTo(statuses)))
            }

        assertThat(fakeEventBus.countEventsOfType(VideosUpdated::class.java)).isEqualTo(1)

        val videosUpdated = fakeEventBus.getEventsOfType(VideosUpdated::class.java).first().videos.map { it.id.value }
        assertThat(videosUpdated).containsExactlyInAnyOrder(
            videoIds[0],
            videoIds[1]
        )
    }

    @Test
    fun `it sorts news by releaseDate descending`() {
        val today = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            type = ContentType.NEWS
        ).value
        val yesterday = saveVideo(
            title = "Yesterday Video",
            date = LocalDate.now().minusDays(1).toString(),
            type = ContentType.NEWS
        ).value
        val tomorrow = saveVideo(
            title = "Tomorrow Video",
            date = LocalDate.now().plusDays(1).toString(),
            type = ContentType.NEWS
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
    fun `it allows sorting randomly`() {
        mockMvc.perform(
            get("/v1/videos?q=video&sort_by=RANDOM")
                .contentType(MediaType.APPLICATION_JSON).asBoclipsEmployee()
        ).andExpect(status().isOk)
    }

    @Test
    fun `transcripts endpoint causes the file to download, without applying formatting`() {
        val videoId = saveVideoWithTranscript("Some content in the video.\n\nThis is another sentence that was said")

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                content().string(
                    equalTo(
                        "Some content in the video.\n" +
                            "\n" +
                            "This is another sentence that was said"
                    )
                )
            )
            .andExpect(header().string("Content-Disposition", equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `transcripts endpoint causes the file to download, applying formatting`() {
        val videoId = saveVideoWithTranscript("Some content in the video. This is another sentence that was said")

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                content().string(
                    equalTo(
                        "Some content in the video.\n" +
                            "\n" +
                            "This is another sentence that was said"
                    )
                )
            )
            .andExpect(header().string("Content-Disposition", equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `going to the transcripts endpoint for a video without transcripts returns 404`() {
        val videoId = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            type = ContentType.NEWS
        ).value

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isNotFound)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `it returns a transcript URI when there is a transcript to download`() {
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
            type = ContentType.NEWS
        ).value

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$._links.transcript.href").doesNotHaveJsonPath())
    }

    @Test
    fun `it updates the subjects of the given video`() {
        val sampleSubject1 = saveSubject("Design")
        val sampleSubject2 = saveSubject("Art")

        val videoToUpdate = saveVideo(
            playbackId = PlaybackId(value = "subject-test", type = PlaybackProviderType.YOUTUBE),
            title = "subject video",
            description = "this video got disabled because it offended Jose Carlos Valero Sanchez",
            date = "2019-01-01",
            subjectIds = setOf(sampleSubject1.id.value, sampleSubject2.id.value),
            duration = Duration.ofSeconds(6),
            contentProvider = "max",
            ageRange = UnboundedAgeRange
        ).value

        val newSubject = saveSubject("Maths")

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate?subjectIds=${newSubject.id.value},${sampleSubject2.id.value}")
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subjects[0].name", equalTo("Art")))
            .andExpect(jsonPath("$.subjects[1].name", equalTo("Maths")))
            .andExpect(jsonPath("$.subjects", hasSize<Int>(2)))
    }

    private fun getRatingLink(videoId: String): String {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        return JsonPath.parse(videoResponse).read("$._links.rate.href")
    }

    private fun getUpdateLink(videoId: String): UriTemplate {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val link = JsonPath.parse(videoResponse).read<String>("$._links.update.href")

        return UriTemplate.fromTemplate(link)
    }

    private fun getTaggingLink(videoId: String): String {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val tagUrl = JsonPath.parse(videoResponse).read<String>("$._links.tag.href")
        return tagUrl
    }

    private fun createTag(name: String): ResultActions {
        return mockMvc.perform(
            post("/v1/tags").content(
                """
                {
                  "label": "$name",
                  "UserId": "User-1"
                }
                """.trimIndent()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
    }

    private fun setPromoted(videoId: String, promoted: Boolean): ResultActions {
        val updateLink = getUpdateLink(videoId).expand(mapOf("promoted" to promoted))

        return mockMvc.perform(patch(URI.create(updateLink)).asBoclipsEmployee())
    }

    private fun saveVideoWithTranscript(transcriptContent: String = "Some content in the video"): String {
        val videoId = saveVideo(
            title = "Today Video?",
            date = LocalDate.now().toString(),
            type = ContentType.NEWS
        ).value

        assertNotNull(
            mongoVideosCollection().findOneAndUpdate(
                eq("title", "Today Video?"),
                set("transcript", transcriptContent)
            )
        )
        return videoId
    }

    private fun removeDistributionMethods(videoId: String) {
        bulkUpdateVideo.invoke(
            bulkUpdateRequest = BulkUpdateRequest(
                ids = listOf(videoId),
                distributionMethods = emptySet()
            )
        )
    }

    private fun mongoVideosCollection() = mongoClient.getDatabase(DATABASE_NAME).getCollection(collectionName)

    private fun setRating(videoId: VideoId, rating: Int) {
        val rateUrl = getRatingLink(videoId.value)

        mockMvc.perform(patch(rateUrl, rating).asTeacher()).andExpect(status().isOk)
    }
}

