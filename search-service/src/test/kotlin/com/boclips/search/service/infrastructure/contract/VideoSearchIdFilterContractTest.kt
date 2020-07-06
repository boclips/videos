package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchIdFilterContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can limit search by permitted ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hai"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    permittedVideoIds = setOf("1", "2")
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not limit search when permitted ids is null`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hai"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    permittedVideoIds = null
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not limit search when permitted ids is empty`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hai"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    permittedVideoIds = emptySet()
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can limit search when looking up by id`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = setOf("1", "2"),
                    permittedVideoIds = setOf("1", "3")
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("1")
        Assertions.assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not include denied video ids in id lookup`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = setOf("1", "2", "3"),
                    deniedVideoIds = setOf("1")
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("3", "2")
        Assertions.assertThat(results.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not include denied video ids in search query`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hello"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    deniedVideoIds = setOf("1", "2")
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactly("3")
        Assertions.assertThat(results.counts.totalHits).isEqualTo(1)
    }
}
