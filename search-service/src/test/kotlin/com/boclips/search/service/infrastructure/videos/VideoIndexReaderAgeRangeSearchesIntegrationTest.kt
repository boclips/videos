package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderAgeRangeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "Pre-school", ageRangeMin = 3, ageRangeMax = 5),
                SearchableVideoMetadataFactory.create(id = "Lower-Elementary", ageRangeMin = 5, ageRangeMax = 7),
                SearchableVideoMetadataFactory.create(id = "Upper-Elementary", ageRangeMin = 7, ageRangeMax = 11),
                SearchableVideoMetadataFactory.create(id = "Middle-School", ageRangeMin = 11, ageRangeMax = 14),
                SearchableVideoMetadataFactory.create(
                    id = "Middle-School-And-Up",
                    ageRangeMin = 11,
                    ageRangeMax = null
                ),
                SearchableVideoMetadataFactory.create(
                    id = "Middle-School-And-Down",
                    ageRangeMin = null,
                    ageRangeMax = 14
                ),
                SearchableVideoMetadataFactory.create(id = "Jr-High-School", ageRangeMin = 14, ageRangeMax = 16),
                SearchableVideoMetadataFactory.create(id = "High-School", ageRangeMin = 16, ageRangeMax = null)
            )
        )
    }

    @Test
    fun `Filtering across a single bracket`() {
        val videoIds = getSearchResults(VideoQuery(ageRangeMin = 7, ageRangeMax = 11))

        assertThat(videoIds).hasSize(1)
        assertThat(videoIds).doesNotContain("Pre-school")
        assertThat(videoIds).doesNotContain("Lower-Elementary")
        assertThat(videoIds).contains("Upper-Elementary")
        assertThat(videoIds).doesNotContain("Middle-School-And-Down")
        assertThat(videoIds).doesNotContain("Middle-School")
        assertThat(videoIds).doesNotContain("Middle-School-And-Up")
        assertThat(videoIds).doesNotContain("Jr-High-School")
    }

    @Test
    fun `Filter across two brackets`() {
        val videoIds = getSearchResults(VideoQuery(ageRangeMin = 7, ageRangeMax = 14))

        assertThat(videoIds).hasSize(2)
        assertThat(videoIds).doesNotContain("Pre-school")
        assertThat(videoIds).doesNotContain("Lower-Elementary")
        assertThat(videoIds).contains("Upper-Elementary")
        assertThat(videoIds).doesNotContain("Middle-School-And-Down")
        assertThat(videoIds).contains("Middle-School")
        assertThat(videoIds).doesNotContain("Middle-School-And-Up")
        assertThat(videoIds).doesNotContain("Jr-High-School")
    }

    @Test
    fun `Filtering across three brackets`() {
        val videoIds = getSearchResults(VideoQuery(ageRangeMin = 7, ageRangeMax = 16))

        assertThat(videoIds).hasSize(3)
        assertThat(videoIds).doesNotContain("Pre-school")
        assertThat(videoIds).doesNotContain("Lower-Elementary")
        assertThat(videoIds).contains("Upper-Elementary")
        assertThat(videoIds).doesNotContain("Middle-School-And-Down")
        assertThat(videoIds).contains("Middle-School")
        assertThat(videoIds).doesNotContain("Middle-School-And-Up")
        assertThat(videoIds).contains("Jr-High-School")
        assertThat(videoIds).doesNotContain("High-School")
    }

    @Test
    fun `Filtering mid bracket only returns videos within the filter`() {
        val videoIds = getSearchResults(VideoQuery(ageRangeMin = 6, ageRangeMax = 16))

        assertThat(videoIds).hasSize(3)
        assertThat(videoIds).doesNotContain("Pre-school")
        assertThat(videoIds).doesNotContain("Lower-Elementary")
        assertThat(videoIds).contains("Upper-Elementary")
        assertThat(videoIds).doesNotContain("Middle-School-And-Down")
        assertThat(videoIds).contains("Middle-School")
        assertThat(videoIds).doesNotContain("Middle-School-And-Up")
        assertThat(videoIds).contains("Jr-High-School")
        assertThat(videoIds).doesNotContain("High-School")
    }

    @Test
    fun `Filtering with max age range returns only videos in brackets below`() {
        val videoIds = getSearchResults(VideoQuery(ageRangeMax = 15))

        assertThat(videoIds).hasSize(5)
        assertThat(videoIds).contains("Pre-school")
        assertThat(videoIds).contains("Lower-Elementary")
        assertThat(videoIds).contains("Upper-Elementary")
        assertThat(videoIds).contains("Middle-School-And-Down")
        assertThat(videoIds).contains("Middle-School")
        assertThat(videoIds).doesNotContain("Middle-School-And-Up")
        assertThat(videoIds).doesNotContain("Jr-High-School")
        assertThat(videoIds).doesNotContain("High-School")
    }

    @Test
    fun `Filtering with lower bound returns only videos in brackets above`() {
        val videoIds = getSearchResults(VideoQuery(ageRangeMin = 7))

        assertThat(videoIds).hasSize(5)
        assertThat(videoIds).doesNotContain("Pre-school")
        assertThat(videoIds).doesNotContain("Lower-Elementary")
        assertThat(videoIds).contains("Upper-Elementary")
        assertThat(videoIds).doesNotContain("Middle-School-And-Down")
        assertThat(videoIds).contains("Middle-School")
        assertThat(videoIds).contains("Middle-School-And-Up")
        assertThat(videoIds).contains("Jr-High-School")
        assertThat(videoIds).contains("High-School")
    }

    private fun getSearchResults(query: VideoQuery) =
        videoIndexReader.search(PaginatedSearchRequest(query = query))
}
