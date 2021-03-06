package com.boclips.videos.service.application.video.search

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import com.boclips.search.service.domain.videos.model.VideoType as SearchVideoType

class RetrievePlayableVideosAccessRulesTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var retrievePlayableVideos: RetrievePlayableVideos

    @Nested
    inner class Searching {
        @Test
        fun `limits search results to downloadable videos only when such access rule is provided`() {
            val firstVideoId = saveVideo(
                title = "access",
                newChannelName = "download-me",
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
            )
            val secondVideoId = saveVideo(
                title = "another access",
                newChannelName = "i-am-open-for-all",
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD, DistributionMethodResource.STREAM)
            )
            saveVideo(
                title = "no access",
                newChannelName = "only-streaming",
                distributionMethods = setOf(DistributionMethodResource.STREAM)
            )

            val searchResults = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    ageRangeStrict = null,
                ),
                VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedDistributionMethods(setOf(DistributionMethod.DOWNLOAD))), emptySet()
                )
            )

            assertThat(searchResults.videos).hasSize(2)
            assertThat(searchResults.videos.map { it.videoId }).containsExactlyInAnyOrder(firstVideoId, secondVideoId)
        }

        @Test
        fun `limits search results when specific id access rule is provided`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    ageRangeStrict = null,
                ),
                VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedIds(setOf(firstVideo))), emptySet()
                )
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(firstVideo)
        }

        @Test
        fun `count takes specific ids access into ac-count (pun intended)`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "access",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    ageRangeStrict = null,
                ),
                VideoAccess.Rules(
                    listOf(VideoAccessRule.IncludedIds(setOf(firstVideo))), emptySet()
                )
            )

            assertThat(searchResults.counts.total).isEqualTo(1)
        }

        @Test
        fun `excluded videos are not returned in search results`() {
            val firstVideo = saveVideo(title = "Wild Elephant")
            val secondVideo = saveVideo(title = "Wild Rhino")

            val searchResults = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    ageRangeStrict = null
                ),
                VideoAccess.Rules(
                    listOf(VideoAccessRule.ExcludedIds(setOf(firstVideo))), emptySet()
                )
            )

            assertThat(searchResults.videos).hasSize(1)
            assertThat(searchResults.videos.map { it.videoId }).containsExactly(secondVideo)
        }

        @Test
        fun `excluded content types are not returned in search results`() {
            saveVideo(title = "Wild Elephant", types = listOf(VideoType.STOCK))
            saveVideo(title = "Wild Elephant", types = listOf(VideoType.NEWS))
            val instructionalVideoId =
                saveVideo(title = "Wild Elephant", types = listOf(VideoType.INSTRUCTIONAL_CLIPS))

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(VideoType.NEWS, VideoType.STOCK))

            val results = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    ageRangeStrict = null
                ),
                VideoAccess.Rules(
                    listOf(accessRule), emptySet()
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `excluded content types are not return in search results even when filtering by an excluded type`() {
            saveVideo(title = "Wild Elephant", types = listOf(VideoType.STOCK))
            saveVideo(title = "Wild Elephant", types = listOf(VideoType.NEWS))
            val instructionalVideoId =
                saveVideo(title = "Wild Elephant", types = listOf(VideoType.INSTRUCTIONAL_CLIPS))

            val accessRule = VideoAccessRule.ExcludedContentTypes(setOf(VideoType.NEWS, VideoType.STOCK))

            val results = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    types = setOf(SearchVideoType.NEWS, SearchVideoType.INSTRUCTIONAL),
                    ageRangeStrict = null
                ),
                VideoAccess.Rules(
                    listOf(accessRule), emptySet()
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(instructionalVideoId)
        }

        @Test
        fun `does not have access to excluded channel`() {
            val allowedContentPartnerId = saveChannel(name = "Tuner").id.value
            val excludedContentPartnerId = saveChannel(name = "Tina").id.value

            val allowedVideoId = saveVideo(title = "Wild Elephant", existingChannelId = allowedContentPartnerId)
            saveVideo(title = "Wild Elephant", existingChannelId = excludedContentPartnerId)
            saveVideo(title = "Wild Elephant", existingChannelId = excludedContentPartnerId)

            val accessRule = VideoAccessRule.ExcludedChannelIds(
                channelIds = setOf(
                    ChannelId(
                        value = excludedContentPartnerId
                    )
                )
            )

            val results = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    types = setOf(SearchVideoType.NEWS, SearchVideoType.INSTRUCTIONAL),
                    ageRangeStrict = null
                ),
                VideoAccess.Rules(
                    listOf(accessRule), emptySet()
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(allowedVideoId)
        }

        @Test
        fun `only has access to included channels`() {
            val allowedChannelId = saveChannel(name = "Tuner").id.value
            val excludedChannelId = saveChannel(name = "Tina").id.value

            val allowedVideoId = saveVideo(title = "Wild Elephant1", existingChannelId = allowedChannelId)
            saveVideo(title = "Wild Elephant2", existingChannelId = excludedChannelId)
            saveVideo(title = "Wild Elephant3", existingChannelId = excludedChannelId)

            val accessRule = VideoAccessRule.IncludedChannelIds(
                channelIds = setOf(
                    ChannelId(
                        value = allowedChannelId
                    )
                )
            )

            val results = retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "Wild",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0),
                    types = setOf(SearchVideoType.NEWS, SearchVideoType.INSTRUCTIONAL),
                    ageRangeStrict = null
                ),
                VideoAccess.Rules(
                    listOf(accessRule), emptySet()
                )
            )

            assertThat(results.videos.map { it.videoId }).containsOnly(allowedVideoId)
        }
    }

    @Test
    fun `access rules take precedence over user query`() {
        saveVideo(title = "Wild Elephant", types = listOf(VideoType.INSTRUCTIONAL_CLIPS))

        val accessRule = VideoAccessRule.IncludedContentTypes(setOf(VideoType.NEWS))

        val results = retrievePlayableVideos.searchPlayableVideos(
            VideoRequest(
                text = "Elephant",
                pageSize = 10,
                pagingState = VideoRequestPagingState.PageNumber(0),
                types = setOf(SearchVideoType.INSTRUCTIONAL),
                ageRangeStrict = null
            ),
            VideoAccess.Rules(
                listOf(accessRule), emptySet()
            )
        )

        assertThat(results.videos).hasSize(0)
    }
}
