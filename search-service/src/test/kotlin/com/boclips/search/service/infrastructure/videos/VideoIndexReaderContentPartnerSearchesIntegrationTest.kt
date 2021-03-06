package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.Month

class VideoIndexReaderContentPartnerSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `content partner matches exactly and on type`() {
        val contentProvider = "Bozeman Science"

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = contentProvider,
                    types = listOf(VideoType.INSTRUCTIONAL)

                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentProvider = contentProvider,
                    types = listOf(VideoType.NEWS)
                )
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        phrase = contentProvider,
                        userQuery = UserQuery(types = setOf(VideoType.NEWS)),
                        videoAccessRuleQuery = VideoAccessRuleQuery()
                    )
                )
            )

        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `content partner matches exactly and also on best for`() {
        val contentProvider = "Bozeman Science"

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = contentProvider,
                    tags = listOf("education")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentProvider = contentProvider,
                    tags = emptyList()
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    phrase = contentProvider,
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        bestFor = listOf("education")
                    )
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `rank content partner matches above other field matches`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "2", description = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "3", contentProvider = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "4", keywords = listOf("TED-Ed")),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "TED-Ed",
                    description = "TED-Ed",
                    keywords = listOf("TED-Ed")
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(phrase = "Ted-ed", videoAccessRuleQuery = VideoAccessRuleQuery()))
        )

        assertThat(results.elements).startsWith("3")
    }

    @Test
    fun `can filter by duration bound`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    durationSeconds = 1,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    durationSeconds = 5,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    durationSeconds = 9,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    durationSeconds = 15,
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    "TED",
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        durationRanges = listOf(
                            DurationRange(
                                min = Duration.ofSeconds(5),
                                max = Duration.ofSeconds(10)
                            )
                        )
                    )
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `can filter by subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    subjects = setOf(createSubjectMetadata("Maths")),
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf(createSubjectMetadata("History")),
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    "TED",
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        subjectIds = setOf("History")
                    )
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1")
    }

    @Test
    fun `can filter by age range`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    ageRangeMax = 5,
                    ageRangeMin = 2,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    ageRangeMax = 9,
                    ageRangeMin = 14,
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    "TED",
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        ageRangeStrict = AgeRange(2, 5),
                    )
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("0")
    }

    @Test
    fun `can filter by release date`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    releaseDate = LocalDate.of(2019, Month.JANUARY, 10),
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    releaseDate = LocalDate.of(2019, Month.MAY, 10),
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    "TED",
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        releaseDateFrom = LocalDate.of(2019, Month.JANUARY, 1),
                        releaseDateTo = LocalDate.of(2019, Month.FEBRUARY, 1)
                    )
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("0")
    }

    @Test
    fun `can filter by source`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    source = SourceType.BOCLIPS,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    source = SourceType.YOUTUBE,
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        source = SourceType.BOCLIPS
                    )
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("0")
    }
}
