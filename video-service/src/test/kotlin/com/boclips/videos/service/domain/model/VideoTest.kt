package com.boclips.videos.service.domain.model

import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration

class VideoTest {

    @Test
    fun `is not playable when there is no stream information`() {
        assertThat(createVideo(videoPlayback = null).isPlayable()).isFalse()
    }

    @Test
    fun `is playable when there is stream information`() {
        val video = createVideo(
                videoPlayback = VideoPlayback(
                        streamUrl = "x",
                        thumbnailUrl = "x",
                        duration = Duration.ofMinutes(2)
                )
        )

        assertThat(video.isPlayable()).isTrue()
    }

}
