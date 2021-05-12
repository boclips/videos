package com.boclips.videos.service.presentation

import com.boclips.eventbus.domain.video.CaptionsFormat
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.support.Cookies
import com.boclips.videos.service.testsupport.*
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.jayway.jsonpath.JsonPath
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.util.*
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
            duration = Duration.ofMinutes(1),
            contentProvider = "enabled-cp",
            legalRestrictions = "None",
            ageRangeMin = 5,
            ageRangeMax = 7
        ).value

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofMinutes(8),
            contentProvider = "enabled-cp2",
            ageRangeMin = 7,
            ageRangeMax = 10
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended Jose Carlos Valero Sanchez",
            date = "2018-05-10",
            duration = Duration.ofMinutes(5),
            contentProvider = "disabled-cp",
            ageRangeMin = null,
            ageRangeMax = null,
            distributionMethods = emptySet()
        ).value
    }

    @Nested
    inner class GetVideo {
        @Test
        fun `returns 200 for valid video as boclips employee`() {
            mockMvc.perform(get("/v1/videos/$kalturaVideoId").asBoclipsEmployee(email = userAssignedToOrganisation().idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.createdBy", equalTo("enabled-cp")))
                .andExpect(jsonPath("$.channel", equalTo("enabled-cp")))
                .andExpect(jsonPath("$.channelId").exists())
                .andExpect(jsonPath("$.channelVideoId", equalTo("content-partner-video-id-entry-id-123")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").exists())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.maxResolutionAvailable", equalTo(true)))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT1M")))
                .andExpect(jsonPath("$.playback.streamUrl").doesNotExist())
                .andExpect(jsonPath("$.playback.thumbnailUrl").doesNotExist())
                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").isNotEmpty())
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(
                    jsonPath(
                        "$.playback._links.setThumbnailBySecond.href",
                        containsString("/videos/$kalturaVideoId/playback{?thumbnailSecond}")
                    )
                )
                .andExpect(jsonPath("$.playback._links.setThumbnailBySecond.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.videoPreview.href",
                        containsString("/vid_slices/{thumbnailCount}")
                    )
                )
                .andExpect(jsonPath("$.playback._links.videoPreview.templated", equalTo(true)))
                .andExpect(jsonPath("$.types[0].id", equalTo(3)))
                .andExpect(jsonPath("$.types[0].name", equalTo("Instructional Clips")))
                .andExpect(jsonPath("$._links.self.href", containsString("/videos/$kalturaVideoId")))
                .andExpect(
                    jsonPath(
                        "$._links.detailsProjection.href",
                        containsString("/videos/$kalturaVideoId?projection=details")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$._links.fullProjection.href",
                        containsString("/videos/$kalturaVideoId?projection=full")
                    )
                )
                .andExpect(jsonPath("$._links.assets.href", containsString("/videos/$kalturaVideoId/assets")))
                .andExpect(jsonPath("$.ageRange.min", equalTo(5)))
                .andExpect(jsonPath("$.ageRange.max", equalTo(7)))

            mockMvc.perform(get("/v1/videos?query=powerful").asBoclipsEmployee(email = userAssignedToOrganisation().idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].channelVideoId").exists())
                .andExpect(jsonPath("$._embedded.videos[0].channelId").exists())
                .andExpect(jsonPath("$._embedded.videos[0].types[0]").isNotEmpty())
                .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", endsWith("/videos/$kalturaVideoId")))
        }

        @Test
        fun `returns 200 for valid video as anonymous user, shows full video if no sharecode`() {
            mockMvc.perform(
                patch("/v1/videos/$kalturaVideoId")
                    .content("""{ "attachments": [{ "linkToResource": "alex.bagpipes.com", "type": "ACTIVITY", "description": "Amazing description" }] }""".trimIndent())
                    .contentType(MediaType.APPLICATION_JSON)
                    .asBoclipsEmployee()
            )

            mockMvc.perform(get("/v1/videos/$kalturaVideoId"))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.createdBy", equalTo("enabled-cp")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").doesNotExist())
                .andExpect(jsonPath("$.playback.downloadUrl").doesNotExist())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT1M")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.hlsStream.href",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                    )
                )
                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").doesNotExist())
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.editThumbnail").doesNotExist())
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
                .andExpect(jsonPath("$.channelVideoId").doesNotExist())
                .andExpect(jsonPath("$.channelId").doesNotExist())
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.attachments", hasSize<Int>(1)))
        }

        @Test
        fun `anonymous user with valid referer & shareCode can access playback and attachments`() {
            usersClient.add(
                UserResourceFactory.sample(
                    id = "referer-id",
                    shareCode = "valid"
                )
            )

            mockMvc.perform(
                patch("/v1/videos/$kalturaVideoId")
                    .content("""{ "attachments": [{ "linkToResource": "alex.bagpipes.com", "type": "ACTIVITY", "description": "Amazing description" }] }""".trimIndent())
                    .contentType(MediaType.APPLICATION_JSON)
                    .asBoclipsEmployee()
            )

            mockMvc.perform(get("/v1/videos/$kalturaVideoId?referer=referer-id&shareCode=valid"))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").doesNotExist())
                .andExpect(jsonPath("$.playback.downloadUrl").doesNotExist())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT1M")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.hlsStream.href",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                    )
                )
                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").doesNotExist())
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.editThumbnail").doesNotExist())
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.videoPreview.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(
                    jsonPath(
                        "$.playback._links.videoPreview.href",
                        containsString("/vid_slices/{thumbnailCount}")
                    )
                )
                .andExpect(jsonPath("$.playback._links.videoPreview.templated", equalTo(true)))
                .andExpect(jsonPath("$.attachments", hasSize<Int>(1)))
        }

        @Test
        fun `anonymous user with invalid referer & shareCode cannot access attachments, and only sees playback thumbnail`() {
            usersClient.add(
                UserResourceFactory.sample(
                    id = "referer-id",
                    shareCode = "valid"
                )
            )

            mockMvc.perform(
                patch("/v1/videos/$kalturaVideoId")
                    .content("""{ "attachments": [{ "linkToResource": "alex.bagpipes.com", "type": "ACTIVITY", "description": "Amazing description" }] }""".trimIndent())
                    .contentType(MediaType.APPLICATION_JSON)
                    .asBoclipsEmployee()
            )

            mockMvc.perform(get("/v1/videos/$kalturaVideoId?referer=referer-id&shareCode=invalid"))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.playback._links.hlsStream").doesNotExist())
                .andExpect(jsonPath("$.playback._links.thumbnail").exists())
                .andExpect(jsonPath("$.attachments", hasSize<Int>(0)))
        }

        @Test
        fun `returns 200 for valid video as API user`() {
            mockMvc.perform(get("/v1/videos/$kalturaVideoId").asApiUser(email = userAssignedToOrganisation().idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$.title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$.description", equalTo("test description 3")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$.createdBy", equalTo("enabled-cp")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.referenceId").doesNotExist())
                .andExpect(jsonPath("$.playback.type", equalTo("STREAM")))
                .andExpect(jsonPath("$.playback.duration", equalTo("PT1M")))

                .andExpect(jsonPath("$.playback._links.createPlaybackEvent.href", containsString("/events/playback")))
                .andExpect(jsonPath("$.playback._links.download.href").doesNotExist())
                .andExpect(jsonPath("$.playback._links.hlsStream.href", containsString("/entryId/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/entry_id/entry-id-123")))
                .andExpect(jsonPath("$.playback._links.thumbnail.href", containsString("/width/{thumbnailWidth}")))
                .andExpect(jsonPath("$.playback._links.thumbnail.templated", equalTo(true)))
                .andExpect(jsonPath("$.playback._links.editThumbnail").doesNotExist())
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

                .andExpect(jsonPath("$.channelVideoId").doesNotExist())
                .andExpect(jsonPath("$.channelId").doesNotExist())
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())

            mockMvc.perform(get("/v1/videos?query=powerful").asApiUser(email = userAssignedToOrganisation().idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].channelVideoId").doesNotExist())
                .andExpect(jsonPath("$._embedded.videos[0].channelId").doesNotExist())
                .andExpect(jsonPath("$._embedded.videos[0].type").doesNotExist())
                .andExpect(jsonPath("$._embedded.videos[0].status").doesNotExist())
        }

        @Test
        fun `returns 200 for valid youtube video as API user`() {
            mockMvc.perform(get("/v1/videos/$youtubeVideoId").asApiUser())
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(youtubeVideoId)))
                .andExpect(jsonPath("$.title", equalTo("elephants took out jobs")))
                .andExpect(jsonPath("$.description", equalTo("it's a video from youtube")))
                .andExpect(jsonPath("$.releasedOn", equalTo("2017-02-11")))
                .andExpect(jsonPath("$.createdBy", equalTo("enabled-cp2")))
                .andExpect(jsonPath("$.playback.id").exists())
                .andExpect(jsonPath("$.playback.duration", equalTo("PT8M")))
                .andExpect(jsonPath("$.playback.referenceId").doesNotExist())
                .andExpect(jsonPath("$.playback.downloadUrl").doesNotExist())
                .andExpect(jsonPath("$.playback.type", equalTo("YOUTUBE")))
        }

        @Test
        fun `returns full projection with caption processing data`() {
            fakeKalturaClient.requestCaption("entry-id-123")

            mockMvc.perform(get("/v1/videos/$kalturaVideoId?projection=full").asBoclipsEmployee())
                .andExpect(status().isOk)
                .andExpect(halJson())
                .andExpect(jsonPath("$.id", equalTo(kalturaVideoId)))
                .andExpect(jsonPath("$.captionStatus", equalTo("REQUESTED")))
        }

        @Test
        fun `returns links to hls stream and thumbnail`() {
            val playbackId = PlaybackId(PlaybackProviderType.KALTURA, "entry-id-123")
            val videoId = saveVideo(playbackId = playbackId)

            mockMvc.perform(get("/v1/videos/${videoId.value}").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo(videoId.value)))
                .andExpect(
                    jsonPath(
                        "$.playback._links.hlsStream.href",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id-123/format/applehttp/flavorParamIds/487041%2C487071%2C487081%2C487091/protocol/https/video.mp4")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.playback._links.thumbnail.href",
                        equalTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/${playbackId.value}/width/{thumbnailWidth}/vid_slices/3/vid_slice/1")
                    )
                )
        }

        @Test
        fun `sets playback consumer cookie when not already present`() {
            mockMvc.perform(get("/v1/videos/$kalturaVideoId"))
                .andExpect(status().isOk)
                .andExpect(cookie().exists(Cookies.DEVICE_ID))
                .andExpect(cookie().httpOnly(Cookies.DEVICE_ID, true))
                .andExpect(cookie().path(Cookies.DEVICE_ID, "/"))
                .andExpect(cookie().secure(Cookies.DEVICE_ID, true))
                .andExpect(cookie().maxAge(Cookies.DEVICE_ID, Duration.ofDays(365).seconds.toInt()))
        }

        @Test
        fun `does not set a playback consumer cookie if already present`() {
            mockMvc.perform(get("/v1/videos/$kalturaVideoId").cookie(Cookie(Cookies.DEVICE_ID, "a-consumer-id")))
                .andExpect(status().isOk)
                .andExpect(cookie().doesNotExist(Cookies.DEVICE_ID))
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
    }

    @Nested
    inner class RateVideo {
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
    }

    @Nested
    inner class TagVideo {
        @Test
        fun `tags video`() {
            val videoId = saveVideo().value

            val tagVideoUrl = getTaggingLink(videoId)
            val tagUrl = createTag("A tag")

            mockMvc.perform(
                patch(tagVideoUrl).content(tagUrl)
                    .contentType("text/uri-list").asTeacher()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.bestFor[*].label", containsInAnyOrder("A tag")))
                .andExpect(jsonPath("$._links.tag").doesNotExist())

            mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.bestFor[*].label", containsInAnyOrder("A tag")))
                .andExpect(jsonPath("$._links.tag").doesNotExist())
        }

        @Test
        fun `returns bestFor list field on the resource`() {
            val videoId = saveVideo().value

            val tagVideoUrl = getTaggingLink(videoId)
            val tagUrl = createTag("Tag")

            mockMvc.perform(
                patch(tagVideoUrl).content(tagUrl)
                    .contentType("text/uri-list").asTeacher()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.bestFor[*].label", containsInAnyOrder("Tag")))
                .andExpect(jsonPath("$._links.tag").doesNotExist())

            mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.bestFor[*].label", containsInAnyOrder("Tag")))
                .andExpect(jsonPath("$._links.tag").doesNotExist())
        }
    }

    @Nested
    inner class CreateVideo {
        @Test
        fun `create new video`() {
            createMediaEntry(
                id = "entry-$123",
                duration = Duration.ofMinutes(1)
            )

            val channelId = saveChannel().id.value

            val content = """
            {
                "providerVideoId": "1",
                "providerId": "$channelId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
                "playbackId": "entry-$123",
                "playbackProvider": "KALTURA"
            }
            """.trimIndent()

            val createdResourceUrl =
                mockMvc.perform(
                    post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content)
                )
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
            val channelId = saveChannel().id.value

            createMediaEntry(
                id = "entry-$123",
                duration = Duration.ofMinutes(1)
            )

            val content = """
                {
                    "providerVideoId": "1",
                    "providerId": "$channelId",
                    "title": "AP title",
                    "description": "AP description",
                    "releasedOn": "2018-12-04T00:00:00",
                    "duration": 100,
                    "legalRestrictions": "none",
                    "keywords": ["k1", "k2"],
                    "videoTypes": ["INSTRUCTIONAL_CLIPS"],
                    "playbackId": "entry-$123",
                    "playbackProvider": "KALTURA",
                    "subjects": ["${subjectId.value}"]
                }
            """.trimIndent()

            val createdResourceUrl =
                mockMvc.perform(
                    post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content)
                )
                    .andExpect(status().isCreated)
                    .andReturn().response.getHeader("Location")

            mockMvc.perform(get(createdResourceUrl!!).asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.subjects[0].name", equalTo("Maths")))
                .andExpect(jsonPath("$.subjects[0].id").exists())
        }

        @Test
        fun `create new video with a language`() {
            val channelId = saveChannel().id.value

            createMediaEntry(
                id = "entry-$123",
                duration = Duration.ofMinutes(1)
            )

            val content = """
            {
                "providerVideoId": "1",
                "providerId": "$channelId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
                "playbackId": "entry-$123",
                "playbackProvider": "KALTURA",
                "language": "ave"
            }
            """.trimIndent()

            val createdResourceUrl =
                mockMvc.perform(
                    post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content)
                )
                    .andExpect(status().isCreated)
                    .andReturn().response.getHeader("Location")

            mockMvc.perform(get(createdResourceUrl!!).asTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.language.code", equalTo("ave")))
                .andExpect(jsonPath("$.language.displayName", equalTo("Avestan")))
        }

        @Test
        fun `create new video with a category`() {
            taxonomyRepository.create(CategoryFactory.sample(code = "A", description = "A description"))

            createMediaEntry(
                id = "entry-$123",
                duration = Duration.ofMinutes(1)
            )

            val channelId = saveChannel().id.value

            val content = """
            {
                "providerVideoId": "1",
                "providerId": "$channelId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
                "playbackId": "entry-$123",
                "playbackProvider": "KALTURA",
                "categories": ["A"]
            }
            """.trimIndent()

            val createdResourceUrl =
                mockMvc.perform(
                    post("/v1/videos").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(content)
                )
                    .andExpect(status().isCreated)
                    .andReturn().response.getHeader("Location")

            mockMvc.perform(get(createdResourceUrl!!).asBoclipsEmployee())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.taxonomy.manual.categories[*].codeValue", containsInAnyOrder("A")))
                .andExpect(jsonPath("$.taxonomy.channel.categories", hasSize<Int>(0)))
        }

        @Test
        fun `create new video that is voiced`() {
            val channelId = saveChannel().id.value

            createMediaEntry(
                id = "entry-$123",
                duration = Duration.ofMinutes(1)
            )

            val content = """
            {
                "providerVideoId": "1",
                "providerId": "$channelId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
                "playbackId": "entry-$123",
                "playbackProvider": "KALTURA",
                "language": "ave",
                "isVoiced": true
            }
            """.trimIndent()

            val createdResourceUrl =
                mockMvc.perform(
                    post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content)
                )
                    .andExpect(status().isCreated)
                    .andReturn().response.getHeader("Location")

            mockMvc.perform(get(createdResourceUrl!!).asBoclipsEmployee())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.language.code", equalTo("ave")))
                .andExpect(jsonPath("$.language.displayName", equalTo("Avestan")))
                .andExpect(jsonPath("$.isVoiced", equalTo(true)))
        }

        @Test
        fun `returns a helpful error message when request is not valid`() {
            val channelId = saveChannel().id.value

            val content = """
            {
                "providerVideoId": "1",
                "providerId": "$channelId"
            }
            """.trimIndent()

            mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message", containsString("A video title is required")))
        }

        @Test
        fun `returns a CONFLICT and a helpful error message when video already exists`() {
            createMediaEntry(
                id = "entry-$123",
                duration = Duration.ofMinutes(1)
            )

            val channelId = saveChannel(name = "AP").id.value

            val content = """
            {
                "providerVideoId": "1",
                "providerId": "$channelId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
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
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
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
    }

    @Nested
    inner class VideoExists {
        @Test
        fun `video lookup by provider id returns 200 when video exists`() {
            val channel = saveChannel(name = "ted")
            saveVideo(contentProvider = "ted", contentProviderVideoId = "abc")

            mockMvc.perform(
                MockMvcRequestBuilders.head("/v1/channels/${channel.id.value}/videos/abc")
                    .asIngestor()
            )
                .andExpect(status().isOk)
        }

        @Test
        fun `video lookup by provider id returns 404 when video does not exist`() {
            mockMvc.perform(MockMvcRequestBuilders.head("/v1/channels/ted/videos/xyz").asIngestor())
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class GetCsv {
        @Test
        fun `gets a csv with metadata based on provided ids`() {
            val video1 = saveVideo(title = "test name 1")
            val video2 = saveVideo(title = "test name 2")

            mockMvc.perform(
                post("/v1/videos/metadata").contentType(MediaType.APPLICATION_JSON).content(
                    """
                {
                    "ids": ["$video1", "$video2"]
                }
                    """.trimIndent()
                ).asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.metadata[0].title", equalTo("test name 1")))
                .andExpect(jsonPath("$.metadata[0].id", equalTo(video1.value)))
                .andExpect(jsonPath("$.metadata[1].title", equalTo("test name 2")))
                .andExpect(jsonPath("$.metadata[1].id", equalTo(video2.value)))
        }
    }

    @Nested
    inner class VideoAssets {

        @Test
        fun `returns video url assets for Kaltura video`() {
            val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
            val videoId = saveVideo(playbackId = playbackId)

            val captions = TestFactories.createCaptions(
                language = Locale.UK,
                content = "bla bla bla",
                format = CaptionsFormat.SRT,
                autoGenerated = false
            )
            kalturaPlaybackProvider.uploadCaptions(playbackId, captions)

            mockMvc.perform(
                get("/v1/videos/${videoId.value}/assets").asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.downloadVideoUrl", containsString("/asset-download/1.mp4")))
                .andExpect(jsonPath("$.downloadCaptionUrl").exists())
                .andExpect(jsonPath("$.downloadCaptionUrl", containsString("http")))
        }

        @Test
        fun `returns only captionUrl when there's no HD video flavour`() {
            val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
            val videoId = saveVideo(
                height = 1920,
                width = 1080,
                playbackId = playbackId,
                assets = setOf(KalturaFactories.createKalturaAsset(height = 400, width = 600))
            )

            val captions = TestFactories.createCaptions(
                language = Locale.UK,
                content = "bla bla bla",
                format = CaptionsFormat.SRT,
                autoGenerated = false
            )
            kalturaPlaybackProvider.uploadCaptions(playbackId, captions)

            mockMvc.perform(
                get("/v1/videos/${videoId.value}/assets").asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.downloadVideoUrl").doesNotExist())
                .andExpect(jsonPath("$.downloadCaptionUrl").exists())
                .andExpect(jsonPath("$.downloadCaptionUrl", containsString("http")))
        }

        @Test
        fun `returns URL for best resolution asset available when it was the best at ingest`() {
            val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
            val videoId = saveVideo(
                height = 400,
                width = 600,
                playbackId = playbackId,
                assets = setOf(KalturaFactories.createKalturaAsset(height = 400, width = 600))
            )

            val captions = TestFactories.createCaptions(
                language = Locale.UK,
                content = "bla bla bla",
                format = CaptionsFormat.SRT,
                autoGenerated = false
            )
            kalturaPlaybackProvider.uploadCaptions(playbackId, captions)

            mockMvc.perform(
                get("/v1/videos/${videoId.value}/assets").asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.downloadVideoUrl", containsString("/asset-download/1.mp4")))
                .andExpect(jsonPath("$.downloadCaptionUrl").exists())
                .andExpect(jsonPath("$.downloadCaptionUrl", containsString("http")))
        }

        @Test
        fun `returns video url assets with empty captions url for Kaltura video without srt captions`() {
            val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
            val videoId = saveVideo(playbackId = playbackId)

            val captions = TestFactories.createCaptions(
                language = Locale.UK,
                content = "bla bla bla",
                format = CaptionsFormat.WEBVTT
            )
            kalturaPlaybackProvider.uploadCaptions(playbackId, captions)
            mockMvc.perform(
                get("/v1/videos/${videoId.value}/assets").asBoclipsEmployee()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.downloadVideoUrl", containsString("/asset-download/1.mp4")))
                .andExpect(jsonPath("$.downloadCaptionUrl").doesNotExist())
        }

        @Test
        fun `returns 403 when missing DOWNLOAD_VIDEO role`() {
            val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
            val videoId = saveVideo(playbackId = playbackId)

            val captions = TestFactories.createCaptions(language = Locale.UK, content = "bla bla bla")
            kalturaPlaybackProvider.uploadCaptions(playbackId, captions)

            mockMvc.perform(
                get("/v1/videos/${videoId.value}/assets").asReporter()
            )
                .andExpect(status().isForbidden)
        }
    }

    private fun getRatingLink(videoId: String): String {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        return JsonPath.parse(videoResponse).read("$._links.rate.href")
    }

    private fun getTaggingLink(videoId: String): String {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        return JsonPath.parse(videoResponse).read("$._links.tag.href")
    }

    private fun createTag(name: String): String {
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
        ).andReturn().response.getHeader("Location")!!
    }
}
