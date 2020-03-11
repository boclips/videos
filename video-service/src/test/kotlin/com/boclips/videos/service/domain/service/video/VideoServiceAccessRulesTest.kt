package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceAccessRulesTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoService: VideoService

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
