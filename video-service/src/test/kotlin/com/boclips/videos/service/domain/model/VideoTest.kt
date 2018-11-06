package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration

class VideoTest {

    @Test
    fun `is not playable when there is no playback information`() {
        assertThat(createVideo(videoPlayback = null).isPlayable()).isFalse()
    }

    @Test
    fun `is playable when there is playback information`() {
        val video = createVideo(
                videoPlayback = StreamPlayback(
                        streamUrl = "x",
                        thumbnailUrl = "x",
                        duration = Duration.ofMinutes(2)
                )
        )

        assertThat(video.isPlayable()).isTrue()
    }

}
