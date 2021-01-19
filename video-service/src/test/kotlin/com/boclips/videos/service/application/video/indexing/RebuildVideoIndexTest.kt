package com.boclips.videos.service.application.video.indexing

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.contract.VideoIndexFake
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.service.VideoChannelService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RebuildVideoIndexTest {
    lateinit var index: VideoIndex
    lateinit var videoChannelService: VideoChannelService

    val streamableContentPartnerId = TestFactories.aValidId()
    val downloadContentPartnerId = TestFactories.aValidId()
    val bothContentPartnerId = TestFactories.aValidId()

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = VideoIndexFake()

        val channelRepository: ChannelRepository = getMockContentPartnerRepo(
            com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel(
                id = ChannelId(
                    bothContentPartnerId
                ),
                distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD)
            ),
            com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel(
                id = ChannelId(
                    streamableContentPartnerId
                ),
                distributionMethods = setOf(DistributionMethod.STREAM)
            ),
            com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel(
                id = ChannelId(
                    downloadContentPartnerId
                ),
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )
        )

        val organisationsClient = OrganisationsClientFake()

        organisationsClient.add(
            OrganisationResourceFactory.sample(
                deal = OrganisationResourceFactory.sampleDeal(
                    prices = DealResource.PricesResource(
                        videoTypePrices = mapOf("STOCK" to DealResource.PriceResource("666", "USD"))
                    )
                )
            )
        )

        videoChannelService = VideoChannelService(channelRepository)
        index = DefaultVideoSearch(
            inMemorySearchService,
            inMemorySearchService,
            videoChannelService
        )
    }

    @Test
    fun `execute rebuilds search index`() {
        val videoId1 = TestFactories.aValidId()
        val videoId2 = TestFactories.aValidId()
        val videoId3 = TestFactories.aValidId()

        index.upsert(sequenceOf(TestFactories.createVideo(videoId = videoId1)))

        val videoRepository = getMockVideoRepo(
            TestFactories.createVideo(
                videoId = videoId2,
                channelId = com.boclips.videos.service.domain.model.video.channel.ChannelId(
                    bothContentPartnerId
                )
            ),
            TestFactories.createVideo(
                videoId = videoId3,
                channelId = com.boclips.videos.service.domain.model.video.channel.ChannelId(
                    bothContentPartnerId
                )
            )
        )

        val rebuildSearchIndex = RebuildVideoIndex(
            videoRepository,
            index
        )

        rebuildSearchIndex.invoke()

        val searchRequest = PaginatedIndexSearchRequest(
            VideoQuery(
                userQuery = UserQuery(
                    ids = setOf(
                        videoId1,
                        videoId2,
                        videoId3
                    )
                ),
                videoAccessRuleQuery = VideoAccessRuleQuery()
            )
        )
        val results = index.search(searchRequest)

        assertThat(results.elements).doesNotContain(videoId1)
        assertThat(results.elements).contains(videoId2)
        assertThat(results.elements).contains(videoId3)
    }

    @Test
    fun `reindexes all videos`() {
        val streamableVideoId = TestFactories.aValidId()
        val downloadableVideoId = TestFactories.aValidId()

        val videoRepository = getMockVideoRepo(
            TestFactories.createVideo(
                videoId = streamableVideoId,
                channelId = com.boclips.videos.service.domain.model.video.channel.ChannelId(
                    streamableContentPartnerId
                ),
                types = listOf(VideoType.STOCK)
            ),
            TestFactories.createVideo(
                videoId = downloadableVideoId,
                channelId = com.boclips.videos.service.domain.model.video.channel.ChannelId(
                    downloadContentPartnerId
                )
            )
        )

        val rebuildSearchIndex = RebuildVideoIndex(
            videoRepository,
            index
        )

        rebuildSearchIndex.invoke()

        val searchRequest = PaginatedIndexSearchRequest(
            VideoQuery(
                userQuery = UserQuery(
                    ids = setOf(
                        streamableVideoId,
                        downloadableVideoId
                    )
                ),
                videoAccessRuleQuery = VideoAccessRuleQuery()
            )
        )

        val results = index.search(searchRequest)
        assertThat(results.elements).containsExactlyInAnyOrder(streamableVideoId, downloadableVideoId)
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoRepository = mock<VideoRepository> {
            on {
                streamAll(any())
            } doThrow (MongoClientException("Boom"))
        }

        val rebuildSearchIndex = RebuildVideoIndex(
            videoRepository,
            index
        )

        assertThrows<MongoClientException> {
            rebuildSearchIndex()
        }
    }

    private fun getMockVideoRepo(vararg videos: Video): VideoRepository {
        return mock<VideoRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<Video>) -> Unit

                consumer(videos.asSequence())
            }
        }
    }

    private fun getMockContentPartnerRepo(vararg channels: Channel): ChannelRepository {
        return mock() {
            channels.forEach {
                on { findById(it.id) } doReturn it
            }
        }
    }
}
