package com.boclips.videos.service.application.video.indexing

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.contract.VideoIndexFake
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.service.OrganisationService
import com.boclips.videos.service.domain.service.VideoChannelService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.organisation.ApiOrganisationService
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class RebuildVideoIndexTest {
    lateinit var index: VideoIndex
    lateinit var videoChannelService: VideoChannelService
    lateinit var organisationService: OrganisationService
    lateinit var priceComputingService: PriceComputingService

    val streamableContentPartnerId = TestFactories.aValidId()
    val downloadContentPartnerId = TestFactories.aValidId()
    val bothContentPartnerId = TestFactories.aValidId()

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = VideoIndexFake()

        val channelRepository: ChannelRepository = getMockContentPartnerRepo(
            createChannel(
                id = ChannelId(
                    bothContentPartnerId
                ),
                distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD)
            ),
            createChannel(
                id = ChannelId(
                    streamableContentPartnerId
                ),
                distributionMethods = setOf(DistributionMethod.STREAM)
            ),
            createChannel(
                id = ChannelId(
                    downloadContentPartnerId
                ),
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )
        )

        val organisationsClient = OrganisationsClientFake()

        organisationsClient.add(
            OrganisationResourceFactory.sample(
                id = "an-organisation-id",
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

        organisationService = ApiOrganisationService(organisationsClient)

        priceComputingService = PriceComputingService()
    }

    @Test
    fun `execute rebuilds search index`() {
        val upsertedButNotInDatabaseID = TestFactories.aValidId()
        val normalVideo1ID = TestFactories.aValidId()
        val normalVideo2ID = TestFactories.aValidId()
        val customPricedVideoID = TestFactories.aValidId()

        index.upsert(sequenceOf(TestFactories.createVideoWithPrices(TestFactories.createVideo(videoId = upsertedButNotInDatabaseID))))

        val videoRepository = getMockVideoRepo(
            TestFactories.createVideo(
                videoId = normalVideo1ID,
                channelId = com.boclips.videos.service.domain.model.video.channel.ChannelId(
                    bothContentPartnerId
                )
            ),
            TestFactories.createVideo(
                videoId = normalVideo2ID,
                channelId = com.boclips.videos.service.domain.model.video.channel.ChannelId(
                    bothContentPartnerId
                )
            ),
            TestFactories.createVideo(
                videoId = customPricedVideoID,
                types = listOf(VideoType.STOCK)
            )
        )

        val rebuildSearchIndex = RebuildVideoIndex(
            videoRepository,
            index,
            organisationService,
            priceComputingService
        )

        rebuildSearchIndex.invoke()

        val searchRequest = PaginatedIndexSearchRequest(
            VideoQuery(
                userQuery = UserQuery(
                    organisationPriceFilter = "an-organisation-id" to setOf(BigDecimal.ONE)
                ),
                videoAccessRuleQuery = VideoAccessRuleQuery()
            )
        )
        val results = index.search(searchRequest)

        assertThat(results.elements).doesNotContain(upsertedButNotInDatabaseID)
        assertThat(results.elements).contains(normalVideo1ID)
        assertThat(results.elements).contains(normalVideo2ID)
        assertThat(results.elements).contains(customPricedVideoID)
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
            index,
            organisationService,
            priceComputingService
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
            index,
            organisationService,
            priceComputingService
        )

        assertThrows<MongoClientException> {
            rebuildSearchIndex()
        }
    }

    private fun getMockVideoRepo(vararg videos: BaseVideo): VideoRepository {
        return mock<VideoRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<BaseVideo>) -> Unit

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
