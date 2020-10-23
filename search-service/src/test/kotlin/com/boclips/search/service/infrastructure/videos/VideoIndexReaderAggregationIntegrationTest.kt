package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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

                val results = videoIndexReader.search(
                    PaginatedSearchRequest(
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
                            id = "1", title = "Apple banana candy", subjects = setOf(
                                SubjectMetadata(id = "1", name = "French")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "2", title = "candy banana apple", subjects = setOf(
                                SubjectMetadata(id = "2", name = "Maths")
                            ),
                            types = listOf(VideoType.STOCK)
                        ),
                        SearchableVideoMetadataFactory.create(
                            id = "3", title = "banana apple candy", subjects = setOf(
                                SubjectMetadata(id = "3", name = "Literacy")
                            ),
                            types = listOf(VideoType.INSTRUCTIONAL)
                        )
                    )
                )

                val results = videoIndexReader.search(
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = ageRangeBuckets,
                                duration = emptyList(),
                                resourceTypes = emptyList(),
                                includeChannelFacets = false
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
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
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "apple",
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true
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
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            phrase = "apple",
                            userQuery = UserQuery(
                                channelNames = setOf("TED")
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true
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
                    PaginatedSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            userQuery = UserQuery(
                                channelNames = setOf("TED"),
                                ageRanges = listOf(AgeRange(1, 3))
                            ),
                            videoAccessRuleQuery = VideoAccessRuleQuery(),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true
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
                    PaginatedSearchRequest(
                        VideoQuery(
                            phrase = "apple",
                            videoAccessRuleQuery = VideoAccessRuleQuery(
                                excludedTypes = setOf(VideoType.STOCK)
                            ),
                            facetDefinition = FacetDefinition.Video(
                                ageRangeBuckets = null,
                                duration = null,
                                resourceTypes = emptyList(),
                                includeChannelFacets = true
                            )
                        )
                    )
                )

                assertThat(results.counts.totalHits).isEqualTo(2)
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(2)

                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "1", hits = 1))
                assertThat(results.counts.getFacetCounts(FacetType.Channels)).contains(Count(id = "3", hits = 1))
            }
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
                PaginatedSearchRequest(
                    VideoQuery(
                        phrase = "apple",
                        videoAccessRuleQuery = VideoAccessRuleQuery()
                    )
                )
            )

            assertThat(results.counts.totalHits).isEqualTo(1)
            assertThat(results.counts.getFacetCounts(FacetType.Channels)).hasSize(0)
        }
    }
}
