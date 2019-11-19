package com.boclips.videos.service.presentation

import com.boclips.eventbus.events.video.VideoInteractedWith
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.servlet.http.Cookie

class EventControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `posted playback events are being saved`() {
        val videoId = aValidId()

        val content = """{
            "videoId":"$videoId",
            "videoIndex":135,
            "captureTime":"2019-02-21T15:34:37.186Z",
            "playerId":"f249f486-fc04-48f7-7361-4413c13a4183",
            "segmentStartSeconds":1469.128248,
            "segmentEndSeconds":1470.728248
        }""".trimIndent()

        mockMvc.perform(
            post("/v1/events/playback")
                .asTeacher(email = "teacher@gmail.com")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                .cookie(Cookie(Cookies.PLAYBACK_DEVICE, "device-id"))
                .content(content)
        )
            .andExpect(status().isCreated)

        val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.userId).isEqualTo("teacher@gmail.com")
        assertThat(event.videoIndex).isEqualTo(135)
        assertThat(event.playerId).isEqualTo("f249f486-fc04-48f7-7361-4413c13a4183")
        assertThat(event.segmentStartSeconds).isEqualTo(1469L)
        assertThat(event.segmentEndSeconds).isEqualTo(1470L)
        assertThat(event.url).isEqualTo("https://teachers.boclips.com/videos?q=abc")
        assertThat(event.playbackDevice).isEqualTo("device-id")
    }

    @Test
    fun `posted player interaction events are being saved`() {
        val videoId = aValidId()

        val content = """{
            "playerId": "player-id-123",
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
        assertThat(event.playerId).isEqualTo("player-id-123")
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
    fun `playbacks by unauthorized users are saved`() {
        val videoId = aValidId()
        mockMvc.perform(
            post("/v1/events/playback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                    "playerId": "123",
                    "videoId" : "$videoId",
                    "videoIndex" : 3,
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 100,
                    "videoDurationSeconds" : 200,
                    "captureTime" : "2018-01-01T00:00:00.000Z",
                    "searchId" : "srch-123"
                    }""".trimMargin()
                )
        )
            .andExpect(status().isCreated)
    }
}
