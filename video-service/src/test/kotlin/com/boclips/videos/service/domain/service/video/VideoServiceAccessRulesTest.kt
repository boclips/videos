package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
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

            val accessRule = VideoAccessRule.IncludedIds(
                setOf(firstVideoId, thirdVideoId)
            )

            val videos = videoService.getPlayableVideos(
                listOf(firstVideoId, secondVideoId),
                VideoAccess.Rules(listOf(accessRule))
            )

            assertThat(videos.map { it.videoId }).containsExactly(firstVideoId)
        }

        @Test
        fun `has access to everything but excluded videos`() {
            val firstVideoId = saveVideo()
            val secondVideoId = saveVideo()
            val thirdVideoId = saveVideo()

            val accessRule = VideoAccessRule.ExcludedIds(
                setOf(firstVideoId, thirdVideoId)
            )

            val videos = videoService.getPlayableVideos(
                listOf(firstVideoId, secondVideoId, thirdVideoId),
                VideoAccess.Rules(listOf(accessRule))
            )

            assertThat(videos.map { it.videoId }).containsExactly(secondVideoId)
        }
    }

    @Nested
    inner class Searching {
        @Test
        fun `limits search results when specific id access rule is provided`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoService.search(
                VideoSearchQuery(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedIds(setOf(firstVideo)))
                )
            )

            assertThat(searchResults).hasSize(1)
            assertThat(searchResults.map { it.videoId }).containsExactly(firstVideo)
        }

        @Test
        fun `count takes specific ids access into ac-count (pun intended)`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoService.count(
                VideoSearchQuery(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedIds(setOf(firstVideo)))
                )
            )

            assertThat(searchResults).isEqualTo(1)
        }

        @Test
        fun `excluded videos are not returned in search results`() {
            val firstVideo = saveVideo(title = "Wild Elephant")
            val secondVideo = saveVideo(title = "Wild Rhino")

            val searchResults = videoService.search(
                VideoSearchQuery(
                    text = "Wild",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(VideoAccessRule.ExcludedIds(setOf(firstVideo)))
                )
            )

            assertThat(searchResults).hasSize(1)
            assertThat(searchResults.map { it.videoId }).containsExactly(secondVideo)
        }
    }
}
