package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoCaptionsCreated
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UpdateCaptionsIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `updates captions of a video`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "entry-id"))

        val captionsCreated = VideoCaptionsCreated.builder()
            .videoId(videoId.value)
            .captions(TestFactories.createCaptions(content = "caption content"))
            .build()

        fakeEventBus.publish(captionsCreated)

        val allVideoCaptions = fakeKalturaClient.getCaptionsForVideo("entry-id")
        assertThat(allVideoCaptions).isNotEmpty

        val uploadedCaptions = fakeKalturaClient.getCaptionContent(allVideoCaptions[0].id)
        assertThat(uploadedCaptions).isEqualTo("caption content")
    }
}
