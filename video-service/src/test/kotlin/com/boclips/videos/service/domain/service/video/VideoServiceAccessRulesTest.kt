package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

        @Test
        fun `has access to everything but excluded content types`() {
            val stockVideoId = saveVideo(type = ContentType.STOCK)
            val newsVideoId = saveVideo(type = ContentType.NEWS)
            val instructionalVideoId = saveVideo(type = ContentType.INSTRUCTIONAL_CLIPS)

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS, ContentType.STOCK))

            val videos = videoService.getPlayableVideos(
                listOf(stockVideoId, newsVideoId, instructionalVideoId), VideoAccess.Rules(
                    listOf(accessRule)
                )
            )

            assertThat(videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `does not have access to excluded content partners`() {
            val allowedContentPartnerId = saveContentPartner(name = "Tina").contentPartnerId.value
            val excludedContentPartnerId = saveContentPartner(name = "Turner").contentPartnerId.value

            val allowedVideoId = saveVideo(contentProviderId = allowedContentPartnerId)
            val firstExcludedVideoId = saveVideo(contentProviderId = excludedContentPartnerId)
            val secondExcludedVideoId = saveVideo(contentProviderId = excludedContentPartnerId)

            val accessRule = VideoAccessRule.ExcludedContentPartners(
                contentPartnerIds = setOf(
                    ContentPartnerId(
                        value = excludedContentPartnerId
                    )
                )
            )

            val videos = videoService.getPlayableVideos(
                listOf(allowedVideoId, firstExcludedVideoId, secondExcludedVideoId),
                VideoAccess.Rules(listOf(accessRule))
            )

            assertThat(videos.map { it.videoId }).containsOnly(allowedVideoId)
        }
    }

    @Nested
    inner class Searching {
        @Test
        fun `video access to everything gives access to both streaming and download`() {
            val streamContentPartner = saveContentPartner(
                name = "stream",
                distributionMethods = setOf(DistributionMethodResource.STREAM)
            )
            val downloadContentPartner = saveContentPartner(
                name = "download",
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
            )

            saveVideo(title = "video", contentProviderId = streamContentPartner.contentPartnerId.value)
            saveVideo(title = "video", contentProviderId = downloadContentPartner.contentPartnerId.value)

            val searchResults = videoService.search(
                request = VideoRequest(
                    text = "video",
                    pageSize = 10,
                    pageIndex = 0
                ),
                videoAccess = VideoAccess.Everything
            )

            assertThat(searchResults.videos).hasSize(2)
        }

        @Test
        fun `limits search results when specific id access rule is provided`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoService.search(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(setOf(firstVideo)),
                        VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.STREAM))
                    )
                )
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(firstVideo)
        }

        @Test
        fun `count takes specific ids access into ac-count (pun intended)`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoService.search(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(setOf(firstVideo)),
                        VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.STREAM))
                    )
                )
            )

            assertThat(searchResults.counts.total).isEqualTo(1)
        }

        @Test
        fun `excluded videos are not returned in search results`() {
            val firstVideo = saveVideo(title = "Wild Elephant")
            val secondVideo = saveVideo(title = "Wild Rhino")

            val searchResults = videoService.search(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(setOf(firstVideo)),
                        VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.STREAM))
                    )
                )
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(secondVideo)
        }

        @Test
        fun `excluded content types are not returned in search results`() {
            saveVideo(title = "Wild Elephant", type = ContentType.STOCK)
            saveVideo(title = "Wild Elephant", type = ContentType.NEWS)
            val instructionalVideoId = saveVideo(title = "Wild Elephant", type = ContentType.INSTRUCTIONAL_CLIPS)

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS, ContentType.STOCK))
            val streamRule = VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.STREAM))

            val results = videoService.search(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(accessRule, streamRule)
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `excluded content types are not returned in search results even when filtering by an excluded type`() {
            saveVideo(title = "Wild Elephant", type = ContentType.STOCK)
            saveVideo(title = "Wild Elephant", type = ContentType.NEWS)
            val instructionalVideoId = saveVideo(title = "Wild Elephant", type = ContentType.INSTRUCTIONAL_CLIPS)

            val contentTypeRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS, ContentType.STOCK))
            val distributionRule = VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.STREAM))

            val results = videoService.search(
                request = VideoRequest(
                    text = "Wild",
                    type = setOf(VideoType.NEWS, VideoType.INSTRUCTIONAL),
                    pageSize = 10,
                    pageIndex = 0
                ),
                videoAccess = VideoAccess.Rules(listOf(contentTypeRule, distributionRule))
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `does not have access to excluded content partners`() {
            val allowedContentPartnerId = saveContentPartner(name = "Tuner").contentPartnerId.value
            val excludedContentPartnerId = saveContentPartner(name = "Tina").contentPartnerId.value

            val allowedVideoId = saveVideo(title = "Wild Elephant", contentProviderId = allowedContentPartnerId)
            saveVideo(title = "Wild Elephant", contentProviderId = excludedContentPartnerId)
            saveVideo(title = "Wild Elephant", contentProviderId = excludedContentPartnerId)

            val contentPartnerRule = VideoAccessRule.ExcludedContentPartners(
                contentPartnerIds = setOf(
                    ContentPartnerId(
                        value = excludedContentPartnerId
                    )
                )
            )

            val streamRule = VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.STREAM))

            val results = videoService.search(
                VideoRequest(
                    text = "Wild",
                    type = setOf(VideoType.NEWS, VideoType.INSTRUCTIONAL),
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccess.Rules(
                    listOf(contentPartnerRule, streamRule)
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(allowedVideoId)
        }
    }

    @Nested
    inner class SingleVideoLookup {
        @Test
        fun `looking up a single video respects access rules`() {
            val stockVideo = saveVideo(title = "Wild Elephant", type = ContentType.STOCK)

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(ContentType.STOCK))

            assertThrows<VideoNotFoundException> {
                videoService.getPlayableVideo(stockVideo, VideoAccess.Rules(listOf(accessRule)))
            }
        }
    }
}
