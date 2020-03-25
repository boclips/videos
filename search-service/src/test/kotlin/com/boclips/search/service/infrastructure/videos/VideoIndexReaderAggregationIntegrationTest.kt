package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoIndexReaderAggregationIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Nested
    inner class AggregationCounts {
        @Nested
        inner class SubjectFacet {
            @Test
            fun `returns counts for all subjects without filter`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", subjects = setOf(
                                SubjectMetadata(id = "1", name = "French"),
                                SubjectMetadata(id = "2", name = "Maths")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple", subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy", subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            )
                        )
                    )
                )

                val results = videoIndexReader.search(PaginatedSearchRequest(query = VideoQuery(phrase = "apple")))

                assertThat(results.counts.totalHits).isEqualTo(3)
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).hasSize(3)

                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "2", hits = 2))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "3", hits = 1))
            }

            @Test
            fun `returns counts for all subjects with filter`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", subjects = setOf(
                                SubjectMetadata(id = "1", name = "French")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple", subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy", subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            )
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            phrase = "apple",
                            subjectIds = setOf("1")
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).hasSize(3)

                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "2", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "3", hits = 1))
            }

            @Test
            fun `returns counts subjects when another filter is applied`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", subjects = setOf(
                                SubjectMetadata(id = "1", name = "French"),
                                SubjectMetadata(id = "2", name = "Maths")
                            ),
                            ageRangeMax = 3, ageRangeMin = 1
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple", subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            ),
                            ageRangeMin = 13, ageRangeMax = 18
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy", subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            ageRangeMin = 13,
                            ageRangeMax = 17
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            subjectIds = setOf("1"),
                            ageRanges = listOf(AgeRange(1, 3))
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "2", hits = 1))
            }
        }

        @Nested
        inner class AgeRangeFacet {

            @Test
            fun `returns counts for age range buckets without filter - bounded age ranges`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", ageRangeMin = 3, ageRangeMax = 9
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "Apple candy", ageRangeMin = 11, ageRangeMax = 14
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "Another apple", ageRangeMin = 13, ageRangeMax = 16
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4", title = "Banana apple", ageRangeMin = 3, ageRangeMax = 5
                        )
                    )
                )

                val results = videoIndexReader.search(PaginatedSearchRequest(query = VideoQuery(phrase = "apple")))

                assertThat(results.counts.totalHits).isEqualTo(4)
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "3-5", hits = 2))
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "5-9", hits = 2))
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "9-11",
                        hits = 1
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "11-14",
                        hits = 2
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "14-16",
                        hits = 2
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "16+", hits = 1))
            }

            @Test
            fun `returns counts for age range buckets without filter - semi-bounded ageRanges`() {
                val videos = sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1", title = "Apple banana candy",
                        ageRangeMin = 3, ageRangeMax = 9
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2", title = "candy banana apple",
                        ageRangeMin = 11, ageRangeMax = null
                    )
                )

                videoIndexWriter.upsert(videos)
                val results = videoIndexReader.search(PaginatedSearchRequest(query = VideoQuery(phrase = "apple")))

                assertThat(results.counts.totalHits).isEqualTo(2)

                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "3-5", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "5-9", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "9-11",
                        hits = 1
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "11-14",
                        hits = 1
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "14-16",
                        hits = 1
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "16+", hits = 1))
            }

            @Test
            fun `returns counts for age range buckets without filter - unbounded age ranges`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", ageRangeMin = null, ageRangeMax = null
                        )
                    )
                )

                val results = videoIndexReader.search(PaginatedSearchRequest(query = VideoQuery(phrase = "apple")))

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "3-5", hits = 0))
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "5-9", hits = 0))
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "9-11",
                        hits = 0
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "11-14",
                        hits = 0
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(
                    Count(
                        id = "14-16",
                        hits = 0
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.AgeRanges)).contains(Count(id = "16+", hits = 0))
            }
        }
    }
}