package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.*
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Duration

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
                            id = "1", title = "Apple banana candy",
                            subjects = setOf(
                                SubjectMetadata(id = "1", name = "French"),
                                SubjectMetadata(id = "2", name = "Maths")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple",
                            subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            )
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple"
                        )
                    )
                )

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
                            id = "1", title = "Apple banana candy",
                            subjects = setOf(
                                SubjectMetadata(id = "1", name = "French")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple",
                            subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            )
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                subjectIds = setOf("1")
                            )
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
                            id = "1", title = "Apple banana candy",
                            subjects = setOf(
                                SubjectMetadata(id = "1", name = "French"),
                                SubjectMetadata(id = "2", name = "Maths")
                            ),
                            ageRangeMax = 3, ageRangeMin = 1
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple",
                            subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            ),
                            ageRangeMin = 13, ageRangeMax = 18
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            ageRangeMin = 13,
                            ageRangeMax = 17
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            userQuery = UserQuery(subjectIds = setOf("1"), ageRanges = listOf(AgeRange(1, 3))),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "2", hits = 1))
            }

            @Test
            fun `access rule constraints are applied for facet count`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy",
                            subjects = setOf(
                                SubjectMetadata(id = "1", name = "French")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple",
                            subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            ),
                            types = listOf(VideoType.STOCK)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            videoAccessRuleQuery = VideoAccessRuleQuery(
                                excludedTypes = setOf(VideoType.STOCK)
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Subjects)).contains(Count(id = "3", hits = 1))
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

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple"
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(4)

                val facetCounts = results.counts.getFacetCounts(FacetType.AgeRanges)
                assertThat(facetCounts).contains(Count(id = "3-5", hits = 2))
                assertThat(facetCounts).contains(Count(id = "5-9", hits = 1))
                assertThat(facetCounts).contains(Count(id = "9-11", hits = 0))
                assertThat(facetCounts).contains(Count(id = "11-14", hits = 2))
                assertThat(facetCounts).contains(Count(id = "14-16", hits = 1))
                assertThat(facetCounts).contains(Count(id = "16-99", hits = 0))
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
                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple"
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)

                val facetCounts = results.counts.getFacetCounts(FacetType.AgeRanges)
                assertThat(facetCounts).contains(Count(id = "3-5", hits = 1))
                assertThat(facetCounts).contains(Count(id = "5-9", hits = 1))
                assertThat(facetCounts).contains(Count(id = "9-11", hits = 0))
                assertThat(facetCounts).contains(Count(id = "11-14", hits = 1))
                assertThat(facetCounts).contains(Count(id = "14-16", hits = 1))
                assertThat(facetCounts).contains(Count(id = "16-99", hits = 1))
            }

            @Test
            fun `returns counts for age range buckets without filter - custom ageRange buckets`() {
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

                val ageRangeBuckets: List<AgeRange> = listOf(
                    AgeRange(3, 10),
                    AgeRange(10, 20)
                )

                videoIndexWriter.upsert(videos)
                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = ageRangeBuckets,
                                duration = emptyList(),
                                resourceTypes = emptyList(),
                                includeChannelFacets = false,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)

                val facetCounts = results.counts.getFacetCounts(FacetType.AgeRanges)
                assertThat(facetCounts).contains(Count(id = "3-10", hits = 1))
                assertThat(facetCounts).contains(Count(id = "10-20", hits = 1))
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

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple"
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)

                val facetCounts = results.counts.getFacetCounts(FacetType.AgeRanges)
                assertThat(facetCounts).contains(Count(id = "3-5", hits = 0))
                assertThat(facetCounts).contains(Count(id = "5-9", hits = 0))
                assertThat(facetCounts).contains(Count(id = "9-11", hits = 0))
                assertThat(facetCounts).contains(Count(id = "11-14", hits = 0))
                assertThat(facetCounts).contains(Count(id = "14-16", hits = 0))
                assertThat(facetCounts).contains(Count(id = "16-99", hits = 0))
            }

            @Test
            fun `returns counts for age range buckets with other filters active`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            ageRangeMin = 3,
                            ageRangeMax = 9,
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4",
                            title = "Banana apple",
                            ageRangeMin = 3,
                            ageRangeMax = 5,
                            types = listOf(VideoType.STOCK)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "Apple candy",
                            ageRangeMin = 11,
                            ageRangeMax = 14,
                            types = listOf(VideoType.INSTRUCTIONAL)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                types = setOf(VideoType.STOCK)
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)

                val facetCounts = results.counts.getFacetCounts(FacetType.AgeRanges)
                assertThat(facetCounts).contains(Count(id = "3-5", hits = 1))
                assertThat(facetCounts).contains(Count(id = "5-9", hits = 0))
                assertThat(facetCounts).contains(Count(id = "9-11", hits = 0))
                assertThat(facetCounts).contains(Count(id = "11-14", hits = 0))
                assertThat(facetCounts).contains(Count(id = "14-16", hits = 0))
                assertThat(facetCounts).contains(Count(id = "16-99", hits = 0))
            }

            @Test
            fun `returns an accurate facet count when we have overlapping min max ranges`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            ageRangeMin = 3,
                            ageRangeMax = 9,
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4",
                            title = "Banana apple",
                            ageRangeMin = 9,
                            ageRangeMax = 11,
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "Apple candy",
                            ageRangeMin = 11,
                            ageRangeMax = 14,
                            types = listOf(VideoType.INSTRUCTIONAL)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                types = setOf(VideoType.INSTRUCTIONAL),
                                ageRanges = listOf(AgeRange(min = 9, max = 11))
                            )
                        )
                    )
                )

                val facetCounts = results.counts.getFacetCounts(FacetType.AgeRanges)

                assertThat(results.elements).hasSize(1)

                assertThat(facetCounts).contains(Count(id = "5-9", hits = 1))
                assertThat(facetCounts).contains(Count(id = "9-11", hits = 1))
                assertThat(facetCounts).contains(Count(id = "11-14", hits = 1))
            }
        }

        @Nested
        inner class DurationFacet {
            @Test
            fun `returns duration buckets when no filters are applied`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            durationSeconds = 70
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "Banana apple",
                            durationSeconds = 130
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "Apple candy",
                            durationSeconds = 310
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                durationRanges = listOf(DurationRange(Duration.ofSeconds(65), Duration.ofSeconds(135)))
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)
                val facetCounts = results.counts.getFacetCounts(FacetType.Duration)

                assertThat(facetCounts).contains(Count(id = "PT0S-PT2M", hits = 1))
                assertThat(facetCounts).contains(Count(id = "PT2M-PT5M", hits = 1))
                assertThat(facetCounts).contains(Count(id = "PT5M-PT10M", hits = 1))
                assertThat(facetCounts).contains(Count(id = "PT10M-PT20M", hits = 0))
                assertThat(facetCounts).contains(Count(id = "PT20M-PT24H", hits = 0))
            }

            @Test
            fun `returns duration buckets when`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            durationSeconds = 70,
                            ageRangeMin = 10,
                            ageRangeMax = 12
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "Banana apple",
                            durationSeconds = 130,
                            ageRangeMin = 5,
                            ageRangeMax = 7
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "Apple candy",
                            durationSeconds = 310,
                            ageRangeMin = 10,
                            ageRangeMax = 12
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                ageRanges = listOf(AgeRange(15, 19)),
                                durationRanges = listOf(DurationRange(Duration.ofSeconds(65), Duration.ofSeconds(135)))
                            )

                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(0)
                val facetCounts = results.counts.getFacetCounts(FacetType.Duration)

                assertThat(facetCounts).contains(Count(id = "PT0S-PT2M", hits = 0))
                assertThat(facetCounts).contains(Count(id = "PT2M-PT5M", hits = 0))
                assertThat(facetCounts).contains(Count(id = "PT5M-PT10M", hits = 0))
                assertThat(facetCounts).contains(Count(id = "PT10M-PT20M", hits = 0))
                assertThat(facetCounts).contains(Count(id = "PT20M-PT24H", hits = 0))
            }
        }

        @Nested
        inner class AttachmentTypesFacet {
            @Test
            fun `returns resource buckets when no filters are applied`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            durationSeconds = 70,
                            attachmentTypes = setOf("Activity")
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "Banana apple",
                            durationSeconds = 130,
                            attachmentTypes = setOf("Lesson Guide")
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "Apple candy",
                            durationSeconds = 310
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple"
                        )
                    )
                )
                assertThat(results.counts.totalHits).isEqualTo(3)
                val facetCounts = results.counts.getFacetCounts(FacetType.AttachmentTypes)

                assertThat(facetCounts).contains(Count(id = "Lesson Guide", hits = 1))
                assertThat(facetCounts).contains(Count(id = "Activity", hits = 1))
            }
        }

        @Nested
        inner class ChannelsFacet {
            @Test
            fun `returns counts for all channel ids without filter`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", contentProvider = "TED", contentPartnerId = "1"
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple", contentProvider = "TED1", contentPartnerId = "2"
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "candy apple", contentProvider = "TED1", contentPartnerId = "2"
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4", title = "banana apple candy", contentProvider = "TED2", contentPartnerId = "3"
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(4)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(3)

                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "2", hits = 2))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "3", hits = 1))
            }

            @Test
            fun `returns counts for all channel ids with filter`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", contentPartnerId = "1", contentProvider = "TED"
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple", contentPartnerId = "2", contentProvider = "TED-ED"
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy", contentPartnerId = "3", contentProvider = "AP"
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                channelIds = setOf("1")
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(3)

                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "2", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "3", hits = 1))
            }

            @Test
            fun `returns channel id counts when another filter is applied`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            contentProvider = "TED",
                            contentPartnerId = "123",
                            ageRangeMin = 1,
                            ageRangeMax = 3
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "candy banana apple",
                            contentProvider = "AP",
                            contentPartnerId = "456",
                            ageRangeMin = 1,
                            ageRangeMax = 3
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "banana apple candy",
                            contentProvider = "AP",
                            contentPartnerId = "456",
                            ageRangeMin = 13,
                            ageRangeMax = 17
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4",
                            title = "banana apple candy",
                            contentProvider = "AP",
                            contentPartnerId = "456",
                            ageRangeMin = 1,
                            ageRangeMax = 3
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            userQuery = UserQuery(
                                channelIds = setOf("123"),
                                ageRanges = listOf(AgeRange(1, 3))
                            ),
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "123", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "456", hits = 2))
            }

            @Test
            fun `access rule constraints are applied for facet count`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy",
                            contentProvider = "TED",
                            contentPartnerId = "1",
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple",
                            contentProvider = "TED-ED",
                            contentPartnerId = "2",
                            types = listOf(VideoType.STOCK)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy",
                            contentProvider = "AP",
                            contentPartnerId = "3",
                            types = listOf(VideoType.INSTRUCTIONAL)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            videoAccessRuleQuery = VideoAccessRuleQuery(
                                excludedTypes = setOf(VideoType.STOCK)
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()

                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "3", hits = 1))
            }

            @Test
            fun `channel facets are not returned when includeChannelFacets is not set to true`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy",
                            contentProvider = "TED",
                            contentPartnerId = "1",
                            types = listOf(VideoType.INSTRUCTIONAL)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(0)
            }

            @Test
            fun `always returns counts for filtered channel ids`() {
                for (i in 1..10) {
                    videoIndexWriter.upsert(
                        sequenceOf(
                            SearchableVideoMetadataFactory.create(
                                id = "banana-${i}",
                                title = "banana candy",
                                contentPartnerId = "channel-id-${i}",
                            ),
                            SearchableVideoMetadataFactory.create(
                                id = "apple-${i}",
                                title = "Apple candy",
                                contentPartnerId = "channel-id-${i}",
                            )
                        ),
                    )
                }

                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "filtering video",
                            title = "cherry candy",
                            contentPartnerId = "channel-id-filtered",
                        )
                    ),
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "candy",
                            userQuery = UserQuery(
                                channelIds = setOf("channel-id-filtered")
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(11)

                assertThat(results.counts.getFacetCounts(FacetType.Channels).size).isEqualTo(11)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(
                    Count(
                        id = "channel-id-filtered",
                        hits = 1
                    )
                )
            }

            @Test
            fun `does not duplicate channel facets when channel filter is selected`() {
                    videoIndexWriter.upsert(
                        sequenceOf(
                            SearchableVideoMetadataFactory.create(
                                id = "banana-1",
                                title = "banana candy",
                                contentPartnerId = "channel-id-1",
                            ),
                        ),
                    )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "candy",
                            userQuery = UserQuery(
                                channelIds = setOf("channel-id-1")
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(1)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "channel-id-1", hits = 1))
            }
        }

        @Nested
        inner class VideoTypes {
            @Test
            fun `returns counts for all video types without filter`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "candy banana apple",
                            types = listOf(VideoType.INSTRUCTIONAL, VideoType.NEWS)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "candy apple", types = listOf(VideoType.NEWS)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4", title = "banana apple candy", types = listOf(VideoType.STOCK)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(4)
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).hasSize(3)

                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(
                    Count(
                        id = "instructional",
                        hits = 2
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(Count(id = "stock", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(Count(id = "news", hits = 2))
            }

            @Test
            fun `returns video type facets with filter applied`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            subjects = setOf(
                                SubjectMetadata(id = "1", name = "Maths")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "candy banana apple",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            types = listOf(VideoType.STOCK)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "banana apple candy",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL, VideoType.NEWS)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4",
                            title = "banana apple candy",
                            subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL, VideoType.STOCK)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            userQuery = UserQuery(
                                subjectIds = setOf("3")
                            ),
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(3)
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).hasSize(3)

                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(Count(id = "news", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(
                    Count(
                        id = "instructional",
                        hits = 2
                    )
                )
            }

            @Test
            fun `facet count respects access rules`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy",
                            contentPartnerId = "1",
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple",
                            contentPartnerId = "2",
                            types = listOf(VideoType.STOCK, VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy",
                            contentPartnerId = "3",
                            types = listOf(VideoType.NEWS)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            videoAccessRuleQuery = VideoAccessRuleQuery(
                                excludedTypes = setOf(VideoType.STOCK)
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList()

                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(
                    Count(
                        id = "instructional",
                        hits = 1
                    )
                )
                assertThat(results.counts.getFacetCounts(FacetType.VideoTypes)).contains(Count(id = "news", hits = 1))
            }
        }

        @Nested
        inner class PricesFacet {
            @Test
            fun `returns counts for all prices without filter`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            prices = mapOf(
                                "the-org-im-in" to BigDecimal.valueOf(1.11),
                                "DEFAULT" to BigDecimal.valueOf(10.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "candy banana apple",
                            prices = mapOf(
                                "the-other-org-1" to BigDecimal.valueOf(20.99),
                                "DEFAULT" to BigDecimal.valueOf(10.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "candy apple",
                            prices = mapOf(
                                "the-other-org-2" to BigDecimal.valueOf(12.99),
                                "DEFAULT" to BigDecimal.valueOf(10.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4",
                            title = "banana apple candy",
                            prices = mapOf(
                                "the-org-im-in" to BigDecimal.valueOf(19.99),
                                "DEFAULT" to BigDecimal.valueOf(10.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "5", title = "123 apple candy", prices = null
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList(),
                                organisationId = "the-org-im-in"
                            )
                        )
                    )
                )


                assertThat(results.counts.totalHits).isEqualTo(5)
                assertThat(results.counts.getFacetCounts(FacetType.Prices)).contains(Count(id = "1099", hits = 2))
                assertThat(results.counts.getFacetCounts(FacetType.Prices)).contains(Count(id = "1999", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Prices)).contains(Count(id = "111", hits = 1))
            }

            // We should think a bit harder what to do in this scenario
            // Ideally these would be ignored, rather than returning a facet of 0 price
            @Test
            fun `returns 0 value price facet for results with no prices`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1", title = "Apple banana candy", prices = null
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList(),
                                organisationId = "the-org-im-in"
                            )
                        )
                    )
                )


                assertThat(results.counts.totalHits).isEqualTo(1)
                assertThat(results.counts.getFacetCounts(FacetType.Prices)).containsExactly(Count(id = "0", hits = 1))
            }

            @Test
            fun `returns counts for all prices and aggregates over default prices when user organisation is not given`() {
                videoIndexWriter.upsert(
                    sequenceOf(
                        SearchableVideoMetadataFactory.create(
                            id = "1",
                            title = "Apple banana candy",
                            prices = mapOf(
                                "some-org-1" to BigDecimal.valueOf(10.99),
                                "DEFAULT" to BigDecimal.valueOf(14.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2",
                            title = "candy banana apple",
                            prices = mapOf(
                                "some-org-2" to BigDecimal.valueOf(20.99),
                                "DEFAULT" to BigDecimal.valueOf(10.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3",
                            title = "candy apple",
                            prices = mapOf(
                                "some-org-3" to BigDecimal.valueOf(10.99),
                                "DEFAULT" to BigDecimal.valueOf(14.99)
                            )
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "4",
                            title = "banana apple candy",
                            prices = mapOf(
                                "some-org-4" to BigDecimal.valueOf(19.99),
                                "DEFAULT" to BigDecimal.valueOf(10.99)
                            )
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true,
                                videoTypes = emptyList(),
                                organisationId = null
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(4)
                assertThat(results.counts.getFacetCounts(FacetType.Prices)).contains(Count(id = "1099", hits = 2))
                assertThat(results.counts.getFacetCounts(FacetType.Prices)).contains(Count(id = "1499", hits = 2))
            }
        }
    }
}
