package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoIndexReaderAgeRangeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "Pre-school",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                ),
                SearchableVideoMetadataFactory.create(
                    id = "Lower-Elementary",
                    ageRangeMin = 5,
                    ageRangeMax = 7
                ),
                SearchableVideoMetadataFactory.create(
                    id = "Upper-Elementary",
                    ageRangeMin = 7,
                    ageRangeMax = 11
                ),
                SearchableVideoMetadataFactory.create(
                    id = "Middle-School",
                    ageRangeMin = 11,
                    ageRangeMax = 14
                ),
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
                SearchableVideoMetadataFactory.create(
                    id = "Jr-High-School",
                    ageRangeMin = 14,
                    ageRangeMax = 16
                ),
                SearchableVideoMetadataFactory.create(
                    id = "High-School",
                    ageRangeMin = 16,
                    ageRangeMax = null
                )
            )
        )
    }

    @Nested
    inner class AgeRangeQueriesWithMinMax {
        @Test
        fun `Filtering across a single bracket`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangeStrict = AgeRange(7, 11)), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(1)

            assertThat(results.elements).contains("Upper-Elementary")
        }

        @Test
        fun `Filter across two brackets`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangeStrict = AgeRange(7, 14)), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(2)
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
        }

        @Test
        fun `Filtering across three brackets`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangeStrict = AgeRange(7, 16)), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(3)
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Jr-High-School")
        }

        @Test
        fun `Filtering mid bracket only returns videos within the filter`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangeStrict = AgeRange(7, 16)), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(3)
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Jr-High-School")
        }

        @Test
        fun `Filtering with max age range returns only videos in brackets below`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangeStrict = AgeRange(max = 15)), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(5)
            assertThat(results.elements).contains("Pre-school")
            assertThat(results.elements).contains("Lower-Elementary")
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School-And-Down")
            assertThat(results.elements).contains("Middle-School")
        }

        @Test
        fun `Filtering with lower bound returns only videos in brackets above`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangeStrict = AgeRange(min = 7)), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(5)
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Middle-School-And-Up")
            assertThat(results.elements).contains("Jr-High-School")
            assertThat(results.elements).contains("High-School")
        }
    }

    @Nested
    inner class AgeRangeQueriesWithRanges {
        @Test
        fun `providing single range with min`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangesRelaxed = listOf(AgeRange(min = 7))), videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(6)

            assertThat(results.elements).contains("Middle-School-And-Down")
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Middle-School-And-Up")
            assertThat(results.elements).contains("Jr-High-School")
            assertThat(results.elements).contains("High-School")
        }

        @Test
        fun `providing single range with min and max`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(ageRangesRelaxed = listOf(AgeRange(min = 7, max = 8))),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(2)

            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School-And-Down")
        }

        @Test
        fun `providing multiple ranges with min and max`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(
                        ageRangesRelaxed = listOf(
                            AgeRange(min = 7, max = 8),
                            AgeRange(min = 11, max = 14)
                        )
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(4)

            assertThat(results.elements).contains("Middle-School-And-Down")
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Middle-School-And-Up")
        }
    }

    @Nested
    inner class AgeRangeBoostResults {
        @Test
        fun `boost bigger age range overlap between query and video`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(
                        ageRangesRelaxed = listOf(AgeRange(min = 7, max = 11))
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(2)
            assertThat(results.elements[0]).isEqualTo("Upper-Elementary")
            assertThat(results.elements[1]).isEqualTo("Middle-School-And-Down")
        }

        @Test
        fun `boost bigger age range overlap between query and video with multiple ageRanges`() {
            val results = getSearchResults(
                VideoQuery(
                    userQuery = UserQuery(
                        ageRangesRelaxed = listOf(AgeRange(min = 7, max = 11), AgeRange(min = 14, max = 16))
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )

            assertThat(results.elements).hasSize(4)

            val topResults = results.elements.subList(0, 1)

            assertThat(topResults).contains("Upper-Elementary")

            val bottomResults = results.elements.subList(1, 4)

            assertThat(bottomResults).contains("Middle-School-And-Down")
            assertThat(bottomResults).contains("Middle-School-And-Up")
            assertThat(bottomResults).contains("Jr-High-School")
        }
    }

    private fun getSearchResults(query: VideoQuery) =
        videoIndexReader.search(PaginatedIndexSearchRequest(query = query))
}
