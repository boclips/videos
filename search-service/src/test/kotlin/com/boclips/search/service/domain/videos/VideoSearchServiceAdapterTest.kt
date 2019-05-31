package com.boclips.search.service.domain.videos

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.InMemoryVideoSearchService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TestVideoSearchService(query: ReadSearchService<VideoMetadata, VideoQuery>, admin: WriteSearchService<VideoMetadata>) :
    VideoSearchServiceAdapter<String>(query, admin) {
    override fun convert(document: String): VideoMetadata {
        return VideoMetadata(
            id = document.substring(0, 1).toUpperCase(),
            title = document,
            description = "",
            contentProvider = "",
            releaseDate = LocalDate.now(),
            keywords = emptyList(),
            tags = listOf("classroom"),
            durationSeconds = 0,
            source = SourceType.YOUTUBE,
            transcript = null
        )
    }
}

class VideoSearchServiceAdapterTest {
    lateinit var searchService: TestVideoSearchService

    @BeforeEach
    internal fun setUp() {
        val inMemorySearchService = InMemoryVideoSearchService()
        searchService = TestVideoSearchService(
            inMemorySearchService,
            inMemorySearchService
        )
    }

    @Test
    fun `upsert one video makes an insert`() {
        searchService.upsert(sequenceOf("hello"))

        val result = searchService.search(
            PaginatedSearchRequest(
            VideoQuery(
                "hello"
            ), 0, 1)
        ).first()

        assertThat(result).isEqualTo("H")
    }

    @Test
    fun `upsert many videos makes an insert`() {
        searchService.upsert(sequenceOf("one", "two"))

        val result = searchService.search(PaginatedSearchRequest(
            VideoQuery(
                "two"
            ), 0, 1)).first()

        assertThat(result).isEqualTo("T")
    }

    @Test
    fun `safeRebuildIndex clears the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.safeRebuildIndex(emptySequence())

        assertThat(searchService.search(PaginatedSearchRequest(
            VideoQuery(
                "hello"
            ), 0, 1))).isEmpty()
    }

    @Test
    fun `count returns document count`() {
        searchService.upsert(sequenceOf("one", "two one"))

        assertThat(searchService.count(VideoQuery("one"))).isEqualTo(2)
    }

    @Test
    fun `removeFromSearch removes from the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.removeFromSearch("H")

        assertThat(searchService.search(PaginatedSearchRequest(
            VideoQuery(
                "hello"
            ), 0, 1))).isEmpty()
    }
}
