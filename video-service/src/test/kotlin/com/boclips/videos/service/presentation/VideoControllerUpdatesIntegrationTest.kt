package com.boclips.videos.service.presentation

import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.*
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

class VideoControllerUpdatesIntegrationTest : AbstractSpringIntegrationTest() {
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

    @Test
    fun `update video metadata`() {
        val videoId = saveVideo(title = "Old title", description = "Old description").value

        mockMvc.perform(
            patch("/v1/videos/$videoId")
                .content("""{ "title": "New title", "description": "New description", "promoted": true }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
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
    fun `other roles are not authorised to add data to a video`() {
        mockMvc.perform(
            post("/v1/videos/99999").asIngestor()
                .contentType(MediaType.APPLICATION_JSON).content("{}")
        )
            .andExpect(status().isForbidden)
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
            ageRangeMin = null,
            ageRangeMax = null
        ).value

        val newSubject = saveSubject("Maths")

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "subjectIds": ["${newSubject.id.value}", "${sampleSubject2.id.value}"] }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subjects", hasSize<Int>(2)))
            .andExpect(jsonPath("$.subjects[0].name", equalTo("Art")))
            .andExpect(jsonPath("$.subjects[1].name", equalTo("Maths")))
    }

    @Test
    fun `updates the age range of a video`() {
        val videoToUpdate = saveVideo(ageRangeMin = 3, ageRangeMax = 10).value

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "ageRangeMin": 4, "ageRangeMax": 12 }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ageRange.min", equalTo(4)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(12)))
            .andExpect(jsonPath("$.ageRange.label", equalTo("4-12")))
    }

    @Test
    fun `updates the age range of a video to an unbounded upper range as body payload`() {
        val videoToUpdate = saveVideo(ageRangeMin = 3, ageRangeMax = 10).value

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "ageRangeMin": 14 }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ageRange.min", equalTo(14)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(null)))
            .andExpect(jsonPath("$.ageRange.label", equalTo("14+")))
    }

    @Test
    fun `updates and replaces the best for tag of a video`() {
        val videoToUpdate = saveVideo().value
        val tagId = saveTag("Brain break")

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "tagId": "$tagId" }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bestFor[0].label", equalTo("Brain break")))

        val replacingTagId = saveTag("Brain smash")

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "tagId": "$replacingTagId" }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bestFor", hasSize<Int>(1)))
            .andExpect(jsonPath("$.bestFor[0].label", equalTo("Brain smash")))
    }

    @Test
    fun `updates a list of attachments`() {
        val videoToUpdate = saveVideo().value

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "attachments": [{ "linkToResource": "alex.bagpipes.com", "type": "ACTIVITY", "description": "Amazing description" }] }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attachments", hasSize<Int>(1)))
            .andExpect(jsonPath("$.attachments[0].id").exists())
            .andExpect(jsonPath("$.attachments[0].description").exists())
            .andExpect(jsonPath("$.attachments[0].type").exists())
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("alex.bagpipes.com")))
    }

    @Test
    fun `updates of an attachment replaces existing attachments`() {
        val videoToUpdate = saveVideo().value

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "attachments": [{ "linkToResource": "ben.chocolatefest.ch", "type": "ACTIVITY", "description": "A less amazing description" }] }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "attachments": [{ "linkToResource": "alex.bagpipes.com", "type": "ACTIVITY", "description": "Amazing description" }] }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attachments", hasSize<Int>(1)))
            .andExpect(jsonPath("$.attachments[0].id").exists())
            .andExpect(jsonPath("$.attachments[0].description").exists())
            .andExpect(jsonPath("$.attachments[0].type").exists())
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("alex.bagpipes.com")))
    }

    @Test
    fun `removes all attachments`() {
        val videoToUpdate = saveVideo().value

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "attachments": [{ "linkToResource": "ben.chocolatefest.ch", "type": "ACTIVITY", "description": "A less amazing description" }] }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            patch("/v1/videos/$videoToUpdate")
                .content("""{ "attachments": null }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/$videoToUpdate").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attachments", hasSize<Int>(0)))
    }

    @Test
    fun `Put request to a update a video's captions endpoint updates its transcript`() {
        val video = saveVideo(title = "Today Video?", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionsFileWithEntryId("playback-id", existingCaptions, "previous captions content").id


        val content = """
            {
               "captions": "WEBVTT FILE\n\n1\n00:01.981 --> 00:04.682\nWe're quite content to be the odd<br>browser out.\n\n2\n00:05.302 --> 00:08.958\nWe don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n\n3\n00:09.526 --> 00:11.324\nWe don't have a profit margin."
            }
        """.trimIndent()

        mockMvc.perform(put("/v1/videos/${video.value}/captions").asBoclipsEmployee()
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isOk)

        mockMvc.perform(get("/v1/videos/${video.value}/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                MockMvcResultMatchers.content().string(
                    equalTo("""
                        We're quite content to be the odd<br>browser out.
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.
                        We don't have a profit margin.""".trimIndent()
                    )
                )
            )
            .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `invalid caption update returns bad request`() {
        val video = saveVideo(title = "Today Video?", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))


        val captionContent = """"DEFORMED WEBVTT FILE\n
                                1\n
                                not a valid cue\n
                                We're quite content to be the odd<br>browser out.\n
                                \n
                                2\n
                                00:05.302 --> 00:08.958\n
                                We don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n
                                \n
                                3\n
                                00:09.526 --> 00:11.324\n
                                We don't have a profit margin."""".trimIndent()


        mockMvc.perform(put("/v1/videos/${video.value}/captions").asBoclipsEmployee()
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
            {
               "captions": "$captionContent"
            }
        """.trimIndent()))
            .andExpect(status().isBadRequest)
    }
}

