package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractVideoAccessRulesIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceAccessRulesTest : AbstractVideoAccessRulesIntegrationTest() {
    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `throws a not found error when it cannot access the video`() {
        val videoId = saveVideo()

        val accessRule = VideoAccessRule.SpecificIds(
            emptySet()
        )

        assertThrows<VideoNotFoundException> { videoService.getPlayableVideo(videoId, accessRule) }
    }

    @Test
    fun `returns video when access rules allow it`() {
        val videoId = saveVideo()

        val accessRule = VideoAccessRule.SpecificIds(
            setOf(videoId)
        )

        val video = videoService.getPlayableVideo(videoId, accessRule)

        assertThat(video.videoId).isEqualTo(videoId)
    }
}