package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoCaptionsCreated
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.messaging.support.MessageBuilder

class UpdateCaptionsIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `updates captions of a video`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "entry-id"))

        val captionsCreated = VideoCaptionsCreated.builder()
            .videoId(videoId.value)
            .captions(TestFactories.createCaptions(content = "caption content"))
            .build()

        val message = MessageBuilder.withPayload(captionsCreated).build()

        subscriptions.videoCaptionsCreated().send(message)

        val allVideoCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("entry-id")
        assertThat(allVideoCaptions).isNotEmpty

        val uploadedCaptions = fakeKalturaClient.getCaptionContentByAssetId(allVideoCaptions[0].id)
        assertThat(uploadedCaptions).isEqualTo("caption content")
    }
}