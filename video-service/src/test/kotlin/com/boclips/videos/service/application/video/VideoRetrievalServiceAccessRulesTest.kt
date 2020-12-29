package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class VideoRetrievalServiceAccessRulesTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRetrievalService: VideoRetrievalService

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

            val videos = videoRetrievalService.getPlayableVideos(
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

            val videos = videoRetrievalService.getPlayableVideos(
                listOf(firstVideoId, secondVideoId, thirdVideoId),
                VideoAccess.Rules(listOf(accessRule))
            )

            assertThat(videos.map { it.videoId }).containsExactly(secondVideoId)
        }

        @Test
        fun `has access to everything but excluded content types`() {
            val stockVideoId = saveVideo(types = listOf(ContentType.STOCK))
            val newsVideoId = saveVideo(types = listOf(ContentType.NEWS))
            val instructionalVideoId = saveVideo(types = listOf(ContentType.INSTRUCTIONAL_CLIPS))

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS, ContentType.STOCK))

            val videos = videoRetrievalService.getPlayableVideos(
                listOf(stockVideoId, newsVideoId, instructionalVideoId),
                VideoAccess.Rules(
                    listOf(accessRule)
                )
            )

            assertThat(videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `does not have access to excluded content partners`() {
            val allowedContentPartnerId = saveChannel(name = "Tina").id.value
            val excludedContentPartnerId = saveChannel(name = "Turner").id.value

            val allowedVideoId = saveVideo(contentProviderId = allowedContentPartnerId)
            val firstExcludedVideoId = saveVideo(contentProviderId = excludedContentPartnerId)
            val secondExcludedVideoId = saveVideo(contentProviderId = excludedContentPartnerId)

            val accessRule = VideoAccessRule.ExcludedChannelIds(
                channelIds = setOf(
                    ChannelId(
                        value = excludedContentPartnerId
                    )
                )
            )

            val videos = videoRetrievalService.getPlayableVideos(
                listOf(allowedVideoId, firstExcludedVideoId, secondExcludedVideoId),
                VideoAccess.Rules(listOf(accessRule))
            )

            assertThat(videos.map { it.videoId }).containsOnly(allowedVideoId)
        }
    }

    @Nested
    inner class SingleVideoLookup {
        @Test
        fun `looking up a single video respects access rules`() {
            val stockVideo = saveVideo(title = "Wild Elephant", types = listOf(ContentType.STOCK))

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.STOCK))

            assertThrows<VideoNotFoundException> {
                videoRetrievalService.getPlayableVideo(stockVideo, VideoAccess.Rules(listOf(accessRule)))
            }
        }
    }
}
