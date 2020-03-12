package com.boclips.videos.service.application.video.indexing

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.contract.VideoSearchServiceFake
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.video.VideoSearchService
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

class RebuildVideoIndexTest {
    lateinit var searchService: VideoSearchService
    lateinit var contentPartnerService: ContentPartnerService

    val streamableContentPartnerId = TestFactories.aValidId()
    val downloadContentPartnerId = TestFactories.aValidId()
    val bothContentPartnerId = TestFactories.aValidId()

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = VideoSearchServiceFake()
        searchService = DefaultVideoSearch(inMemorySearchService, inMemorySearchService)

        val contentPartnerRepository: ContentPartnerRepository = getMockContentPartnerRepo(
            com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartner(
                id = ContentPartnerId(bothContentPartnerId),
                distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD)
            ),
            com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartner(
                id = ContentPartnerId(streamableContentPartnerId),
                distributionMethods = setOf(DistributionMethod.STREAM)
            ),
            com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartner(
                id = ContentPartnerId(downloadContentPartnerId),
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )
        )

        contentPartnerService = ContentPartnerService(contentPartnerRepository)
    }

    @Test
    fun `execute rebuilds search index`() {
        val videoId1 = TestFactories.aValidId()
        val videoId2 = TestFactories.aValidId()
        val videoId3 = TestFactories.aValidId()

        searchService.upsert(sequenceOf(TestFactories.createVideo(videoId = videoId1)))

        val videoRepository = getMockVideoRepo(
            TestFactories.createVideo(
                videoId = videoId2,
                contentPartnerId = com.boclips.videos.service.domain.model.video.ContentPartnerId(bothContentPartnerId)
            ),
            TestFactories.createVideo(
                videoId = videoId3,
                contentPartnerId = com.boclips.videos.service.domain.model.video.ContentPartnerId(bothContentPartnerId)
            )
        )

        val rebuildSearchIndex = RebuildVideoIndex(
            videoRepository,
            contentPartnerService,
            searchService
        )

        rebuildSearchIndex.invoke()

        val searchRequest = PaginatedSearchRequest(
            VideoQuery(
                ids = listOf(
                    videoId1,
                    videoId2,
                    videoId3
                )
            )
        )
        val searchResults = searchService.search(searchRequest)
        assertThat(searchResults).doesNotContain(videoId1)
        assertThat(searchResults).contains(videoId2)
        assertThat(searchResults).contains(videoId3)
    }

    @Test
    fun `only reindexes streamable videos`() {
        val streamableVideoId = TestFactories.aValidId()
        val downloadableVideoId = TestFactories.aValidId()

        val videoRepository = getMockVideoRepo(
            TestFactories.createVideo(
                videoId = streamableVideoId,
                contentPartnerId = com.boclips.videos.service.domain.model.video.ContentPartnerId(
                    streamableContentPartnerId
                )
            ),
            TestFactories.createVideo(
                videoId = downloadableVideoId,
                contentPartnerId = com.boclips.videos.service.domain.model.video.ContentPartnerId(
                    downloadContentPartnerId
                )
            )
        )

        val rebuildSearchIndex = RebuildVideoIndex(
            videoRepository,
            contentPartnerService,
            searchService
        )

        rebuildSearchIndex.invoke()

        val searchRequest = PaginatedSearchRequest(
            VideoQuery(
                ids = listOf(
                    streamableVideoId,
                    downloadableVideoId
                )
            )
        )

        val searchResults = searchService.search(searchRequest)
        assertThat(searchResults).contains(streamableVideoId)
        assertThat(searchResults).doesNotContain(downloadableVideoId)
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
            contentPartnerService,
            searchService
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

    private fun getMockContentPartnerRepo(vararg contentPartners: ContentPartner): ContentPartnerRepository {
        return mock() {
            contentPartners.forEach {
                on { findById(it.contentPartnerId) } doReturn it
            }
        }
    }
}
