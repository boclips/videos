package com.boclips.videos.service.domain.service.video.plackback

import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class PlaybackServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var playbackService: PlaybackService

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `updates videos matching the filter`() {
        val playbackId = TestFactories.createKalturaPlayback().id
        val videoId = saveVideo(
            playbackId = playbackId
        )

        fakeKalturaClient.clear()
        createMediaEntry(
            id = playbackId.value,
            duration = Duration.ofSeconds(1000)
        )

        playbackService.updatePlaybackFor(VideoFilter.IsKaltura)

        val updatedAsset = videoRepository.find(videoId)!!
        Assertions.assertThat(updatedAsset.playback).isNotNull
        Assertions.assertThat(updatedAsset.playback.duration).isEqualTo(Duration.ofSeconds(1000))
    }
}
