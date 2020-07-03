package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.request.VideoRequest
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
                listOf(stockVideoId, newsVideoId, instructionalVideoId), VideoAccess.Rules(
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
    inner class Searching {
        @Test
        fun `always limits search to videos eligible for streaming`() {
            val streamContentPartner = saveChannel(
                name = "stream",
                distributionMethods = setOf(DistributionMethodResource.STREAM)
            )
            val downloadContentPartner = saveChannel(
                name = "download",
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
            )

            val streamVideo =
                saveVideo(title = "video", contentProviderId = streamContentPartner.id.value)
            saveVideo(title = "video", contentProviderId = downloadContentPartner.id.value)

            val searchResults = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "video",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Everything
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(streamVideo)
        }

        @Test
        fun `limits search results when specific id access rule is provided`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedIds(setOf(firstVideo)))
                )
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(firstVideo)
        }

        @Test
        fun `count takes specific ids access into ac-count (pun intended)`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedIds(setOf(firstVideo)))
                )
            )

            assertThat(searchResults.counts.total).isEqualTo(1)
        }

        @Test
        fun `excluded videos are not returned in search results`() {
            val firstVideo = saveVideo(title = "Wild Elephant")
            val secondVideo = saveVideo(title = "Wild Rhino")

            val searchResults = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(VideoAccessRule.ExcludedIds(setOf(firstVideo)))
                )
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(secondVideo)
        }

        @Test
        fun `excluded content types are not returned in search results`() {
            saveVideo(title = "Wild Elephant", types = listOf(ContentType.STOCK))
            saveVideo(title = "Wild Elephant", types = listOf(ContentType.NEWS))
            val instructionalVideoId = saveVideo(title = "Wild Elephant", types = listOf(ContentType.INSTRUCTIONAL_CLIPS))

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS, ContentType.STOCK))

            val results = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(accessRule)
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `excluded content types are not return in search results even when filtering by an excluded type`() {
            saveVideo(title = "Wild Elephant", types = listOf(ContentType.STOCK))
            saveVideo(title = "Wild Elephant", types = listOf(ContentType.NEWS))
            val instructionalVideoId = saveVideo(title = "Wild Elephant", types = listOf(ContentType.INSTRUCTIONAL_CLIPS))

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS, ContentType.STOCK))

            val results = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    types = setOf(VideoType.NEWS, VideoType.INSTRUCTIONAL),
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(accessRule)
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `does not have access to excluded channel`() {
            val allowedContentPartnerId = saveChannel(name = "Tuner").id.value
            val excludedContentPartnerId = saveChannel(name = "Tina").id.value

            val allowedVideoId = saveVideo(title = "Wild Elephant", contentProviderId = allowedContentPartnerId)
            saveVideo(title = "Wild Elephant", contentProviderId = excludedContentPartnerId)
            saveVideo(title = "Wild Elephant", contentProviderId = excludedContentPartnerId)

            val accessRule = VideoAccessRule.ExcludedChannelIds(
                channelIds = setOf(
                    ChannelId(
                        value = excludedContentPartnerId
                    )
                )
            )

            val results = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    types = setOf(VideoType.NEWS, VideoType.INSTRUCTIONAL),
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(accessRule)
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(allowedVideoId)
        }

        @Test
        fun `only has access to included channels`() {
            val allowedChannelId = saveChannel(name = "Tuner").id.value
            val excludedChannelId = saveChannel(name = "Tina").id.value

            val allowedVideoId = saveVideo(title = "Wild Elephant1", contentProviderId = allowedChannelId)
            saveVideo(title = "Wild Elephant2", contentProviderId = excludedChannelId)
            saveVideo(title = "Wild Elephant3", contentProviderId = excludedChannelId)

            val accessRule = VideoAccessRule.IncludedChannelIds(
                channelIds = setOf(
                    ChannelId(
                        value = allowedChannelId
                    )
                )
            )

            val results = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    types = setOf(VideoType.NEWS, VideoType.INSTRUCTIONAL),
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(accessRule)
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(allowedVideoId)
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
