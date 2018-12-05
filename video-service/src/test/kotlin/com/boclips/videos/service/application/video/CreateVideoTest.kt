package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate

class CreateVideoTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var createVideo: CreateVideo

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `requesting creation of an existing video creates the video`() {

        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$123", referenceId = "1234", duration = Duration.ofMinutes(1)))

        val resource = createVideo.execute(CreateVideoRequest(
                provider = "Bloomberg",
                providerVideoId = "123",
                title = "the latest Bloomberg video",
                description = "bloomberg description",
                releasedOn = LocalDate.now(),
                duration = Duration.ofMinutes(1),
                legalRestrictions = "none",
                keywords = listOf("k1"),
                contentType = "NEWS",
                playbackId = "1234",
                playbackProvider = "KALTURA"
        ))

        assertThat(videoService.get(AssetId(resource.id!!))).isNotNull
    }

    @Test
    fun `created video becomes available in search`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$123", referenceId = "1234", duration = Duration.ofMinutes(1)))

        createVideo.execute(CreateVideoRequest(
                provider = "Bloomberg",
                providerVideoId = "123",
                title = "the latest Bloomberg video",
                description = "bloomberg description",
                releasedOn = LocalDate.now(),
                duration = Duration.ofMinutes(1),
                legalRestrictions = "none",
                keywords = listOf("k1"),
                contentType = "NEWS",
                playbackId = "1234",
                playbackProvider = "KALTURA"
        ))

        assertThat(videoService.search(VideoSearchQuery("the latest bloomberg", 0, 1)).first().asset.title).isEqualTo("the latest Bloomberg video")
    }
}
