package com.boclips.videos.service.presentation

import com.boclips.eventbus.events.collection.CollectionInteractedWith
import com.boclips.eventbus.events.collection.CollectionInteractionType
import com.boclips.eventbus.events.video.VideoInteractedWith
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.support.Cookies
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.Cookie

class EventControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Nested
    inner class OverridingUserIdViaHeader {
        @Test
        fun `it uses user id provided via Boclips-User-Id header in the event when organisation allows that`() {
            val userId = aValidId()
            val externalUserId = aValidId()
            val organisationId = aValidId()

            usersClient.add(UserResourceFactory.sample(
                id = userId,
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = organisationId
                )
            ))
            organisationsClient.add(
                OrganisationResourceFactory.sample(
                    id = organisationId,
                    organisationDetails = OrganisationResourceFactory.sampleDetails(
                        allowsOverridingUserIds = true
                    )
                )
            )

            mockMvc.perform(
                post("/v1/events/playback")
                    .asApiUser(email = userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Boclips-User-Id", externalUserId)
                    .content(
                        """{
                            "videoId":"${aValidId()}",
                            "segmentStartSeconds":1469.128248,
                            "segmentEndSeconds":1470.728248
                        }""".trimIndent()
                    )
            )
                .andExpect(status().isCreated)

            val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

            assertThat(event.userId).isEqualTo(userId)
            assertThat(event.externalUserId).isEqualTo(externalUserId)
        }

        @Test
        fun `it does not use id provided in the header if organisation does not allow that`() {
            val videoId = aValidId()
            val userId = aValidId()
            val overrideUserId = aValidId()
            val organisationId = aValidId()

            usersClient.add(UserResourceFactory.sample(
                id = userId,
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = organisationId
                )
            ))
            organisationsClient.add(
                OrganisationResourceFactory.sample(
                    id = organisationId,
                    organisationDetails = OrganisationResourceFactory.sampleDetails(
                        allowsOverridingUserIds = false
                    )
                )
            )
            mockMvc.perform(
                post("/v1/events/playback")
                    .asApiUser(email = userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Boclips-User-Id", overrideUserId)
                    .content(
                        """{
                            "videoId":"$videoId",
                            "videoIndex":135,
                            "segmentStartSeconds":1469.128248,
                            "segmentEndSeconds":1470.728248
                        }""".trimIndent()
                    )
            )
                .andExpect(status().isCreated)

            val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

            assertThat(event.userId).isEqualTo(userId)
        }
    }

    @Nested
    inner class PlaybackEvents {
        @Test
        fun `single playback events is being saved`() {
            val videoId = aValidId()

            val content = """{
            "videoId":"$videoId",
            "videoIndex":135,
            "segmentStartSeconds":1469.128248,
            "segmentEndSeconds":1470.728248
        }""".trimIndent()

            mockMvc.perform(
                post("/v1/events/playback")
                    .asTeacher(email = "teacher@gmail.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                    .cookie(Cookie(Cookies.DEVICE_ID, "device-id"))
                    .content(content)
            )
                .andExpect(status().isCreated)

            val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

            assertThat(event.videoId).isEqualTo(videoId)
            assertThat(event.userId).isEqualTo("teacher@gmail.com")
            assertThat(event.videoIndex).isEqualTo(135)
            assertThat(event.segmentStartSeconds).isEqualTo(1469L)
            assertThat(event.segmentEndSeconds).isEqualTo(1470L)
            assertThat(event.url).isEqualTo("https://teachers.boclips.com/videos?q=abc")
            assertThat(event.playbackDevice).isEqualTo("device-id")
            assertThat(event.deviceId).isEqualTo("device-id")
            assertThat(event.timestamp).isNotNull()
        }

        @Test
        fun `single playback event by unauthorized users is saved`() {
            val videoId = aValidId()
            mockMvc.perform(
                post("/v1/events/playback")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{
                    "videoId" : "$videoId",
                    "videoIndex" : 3,
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 100,
                    "videoDurationSeconds" : 200,
                    "searchId" : "srch-123"
                    }""".trimMargin()
                    )
            )
                .andExpect(status().isCreated)
        }

        @Test
        fun `batched playback events for authorized users are being saved`() {
            val videoId = aValidId()
            mockMvc.perform(
                post("/v1/events/playback/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(email = "teacher@gmail.com")
                    .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                    .content(
                        """[{
                    "videoId" : "$videoId",
                    "videoIndex" : 987,
                    "segmentStartSeconds" : 5,
                    "segmentEndSeconds" : 101,
                    "videoDurationSeconds" : 200,
                    "captureTime": "${ZonedDateTime.of(2019, 11, 18, 0, 0, 0, 0, ZoneOffset.UTC)}",
                    "searchId" : "srch-123"
                    }]""".trimMargin()
                    )
            )
                .andExpect(status().isCreated)

            assertThat(fakeEventBus.countEventsOfType(VideoSegmentPlayed::class.java)).isEqualTo(1)
        }

        @Test
        fun `batched playback events need to be authorized`() {
            mockMvc.perform(
                post("/v1/events/playback/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                    .content(
                        """[]""".trimMargin()
                    )
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `batched invalid playback events return error message`() {
            val videoId = aValidId()

            mockMvc.perform(
                post("/v1/events/playback/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(email = "teacher@gmail.com")
                    .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                    .content(
                        """[{
                    "videoId" : "$videoId",
                    "videoIndex" : 987,
                    "segmentStartSeconds" : 5,
                    "segmentEndSeconds" : 101,
                    "videoDurationSeconds" : 200,
                    "searchId" : "srch-123"
                    },{
                    "videoId" : "$videoId",
                    "videoIndex" : 98,
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 90,
                    "videoDurationSeconds" : 200,
                    "captureTime": "${ZonedDateTime.of(2019, 11, 18, 0, 0, 0, 0, ZoneOffset.UTC)}",
                    "searchId" : "srch-123"
                    }]""".trimMargin()
                    )
            )
                .andExpect(status().`is`(400))
                .andExpect(
                    MockMvcResultMatchers.jsonPath(
                        "$.message",
                        Matchers.containsString("Event captureTime cannot be null")
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath(
                        "$.error",
                        Matchers.containsString("Invalid Event")
                    )
                )
        }
    }

    @Test
    fun `posted player interaction events are being saved`() {
        val videoId = aValidId()

        val content = """{
            "videoId": "$videoId",
            "currentTime": 23,
            "subtype": "captions-on",
            "payload": {
                "kind": "caption-kind",
                "label": "caption-label",
                "language": "caption-language",
                "id": "caption-id"
            }
        }""".trimIndent()

        mockMvc.perform(
            post("/v1/events/player-interaction")
                .asTeacher(email = "teacher@gmail.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)

        val event = fakeEventBus.getEventOfType(VideoPlayerInteractedWith::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.userId).isEqualTo("teacher@gmail.com")
        assertThat(event.currentTime).isEqualTo(23L)
        assertThat(event.subtype).isEqualTo("captions-on")
        assertThat(event.payload).containsEntry("kind", "caption-kind")
        assertThat(event.payload).containsEntry("label", "caption-label")
        assertThat(event.payload).containsEntry("language", "caption-language")
        assertThat(event.payload).containsEntry("id", "caption-id")
    }

    @Test
    fun `posted video interaction events are being saved`() {
        val videoId = saveVideo()
        val videoInteractedWithLink = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturnLink(VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION)
            .expand(mapOf("type" to "COPY_SHARE_LINK"))

        mockMvc.perform(post(videoInteractedWithLink).asTeacher(email = "john@teacher.com"))
            .andExpect(status().isOk)

        val event = fakeEventBus.getEventOfType(VideoInteractedWith::class.java)
        assertThat(event.subtype).isEqualTo("COPY_SHARE_LINK")
        assertThat(event.videoId).isEqualTo(videoId.value)
        assertThat(event.userId).isEqualTo("john@teacher.com")
    }

    @Test
    fun `posted collection interaction events are being saved`() {
        val collectionId = saveCollection(discoverable = true)

        val collectionInteractedWithLink = mockMvc.perform(get("/v1/collections/${collectionId.value}").asTeacher())
            .andExpect(status().isOk)
            .andReturnLink(CollectionsLinkBuilder.Rels.LOG_COLLECTION_INTERACTION)
            .expand()

        mockMvc.perform(
            post(collectionInteractedWithLink)
                .asTeacher(email = "john@teacher.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                    "subtype": "NAVIGATE_TO_COLLECTION_DETAILS"
                    }""".trimMargin()
                )
        )
            .andExpect(status().isOk)

        val event = fakeEventBus.getEventOfType(CollectionInteractedWith::class.java)
        assertThat(event.subtype).isEqualTo(CollectionInteractionType.NAVIGATE_TO_COLLECTION_DETAILS)
        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("john@teacher.com")
    }

    @Test
    fun `posted collection interaction events with a boclips-referer will overwrite existing referer`() {
        val collectionId = saveCollection(discoverable = true)

        val collectionInteractedWithLink = mockMvc.perform(get("/v1/collections/${collectionId.value}").asTeacher())
            .andExpect(status().isOk)
            .andReturnLink(CollectionsLinkBuilder.Rels.LOG_COLLECTION_INTERACTION)
            .expand()

        mockMvc.perform(
            post(collectionInteractedWithLink)
                .asTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Boclips-Referer", "http://www.boclips.com")
                .header("Referer", "http://www.bad-url.com")
                .content(
                    """{
                    "subtype": "NAVIGATE_TO_COLLECTION_DETAILS"
                    }""".trimMargin()
                )
        )
            .andExpect(status().isOk)

        val event = fakeEventBus.getEventOfType(CollectionInteractedWith::class.java)
        assertThat(event.url).isEqualTo("http://www.boclips.com")
    }
}
