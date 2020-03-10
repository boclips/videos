package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceAccessRulesTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `throws a not found error when it cannot access the video`() {
        val videoId = saveVideo()

        val accessRule = VideoAccessRule.SpecificIds(
            setOf(TestFactories.createVideoId())
        )

        assertThrows<VideoNotFoundException> {
            videoService.getPlayableVideo(
                videoId,
                VideoAccess.Rules(listOf(accessRule))
            )
        }
    }

    @Test
    fun `returns video when access rules allow it`() {
        val videoId = saveVideo()

        val accessRule = VideoAccessRule.SpecificIds(
            setOf(videoId)
        )

        val video = videoService.getPlayableVideo(videoId, VideoAccess.Rules(listOf(accessRule)))

        assertThat(video.videoId).isEqualTo(videoId)
    }

    @Nested
    inner class GetPlayableVideos {
        @Test
        fun `limits returned videos to the ones specified in access rule`() {
            val firstVideoId = saveVideo()
            val secondVideoId = saveVideo()
            val thirdVideoId = saveVideo()

            val accessRule = VideoAccessRule.SpecificIds(
                setOf(firstVideoId, thirdVideoId)
            )

            val videos = videoService.getPlayableVideos(
                listOf(firstVideoId, secondVideoId),
                VideoAccess.Rules(listOf(accessRule))
            )

            assertThat(videos.map { it.videoId }).containsExactly(firstVideoId)
        }
    }
}
