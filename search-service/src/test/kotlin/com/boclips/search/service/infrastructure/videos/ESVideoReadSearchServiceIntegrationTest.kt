package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration

class ESVideoReadSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(CONFIG.buildClient())
    }

    @Nested
    inner class QuerySearches {
        @Test
        fun `words appear in sequence in title increases rank`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("Apple banana candy"))
            )

            assertThat(results.first()).isEqualTo("1")
        }

        @Test
        fun `words appear in sequence in description increases rank`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("Apple banana candy"))
            )

            assertThat(results.first()).isEqualTo("1")
        }

        @Test
        fun `matches keywords`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
                )
            )

            val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery("dogs")))

            assertThat(results).containsExactly("2")
        }

        @Test
        fun `content partners must match exactly`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Bozeman Science"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "a video about science")
                )
            )

            val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery(phrase = "science")))

            assertThat(results).containsExactly("2")
        }

        @Test
        fun `transcripts match`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        transcript = "game of thrones season 8 episode 6 online watch free no ads"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        transcript = "the big bang theory season 8 episode 6 online watch free no ads"
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("thrones"))
            )

            assertThat(results).containsExactly("1")
        }

        @Test
        fun `title match is ranked higher than transcript match`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        transcript = "game of thrones season 8 episode 6 online watch free no ads"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "game of thrones season 8 is the best one yet"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        transcript = "game of thrones season 8 is the worst one yet"
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("thrones"))
            )

            assertThat(results).hasSize(3)
            assertThat(results).startsWith("2")
        }

        @Test
        fun `takes stopwords into account for queries like "I have a dream"`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                    SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("i have a dream"))
            )

            assertThat(results).containsExactly("2")
        }

        @Test
        fun `can match word stems eg "it's raining" will match "rain"`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("rain"))
            )

            assertThat(results).containsExactly("1")
        }

        @Test
        fun `exact matches increase rank`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "Royal Australian Regiment and Operation Dalby - a heli-borne assault during Vietnam War, 16th February, 1967",
                        description = "Royal Australian Regiment and Operation Dalby - a heli-borne assault during Vietnam War, 16th February, 1967. Helicopter fleet, POVs from helicopters."
                    ),
                    SearchableVideoMetadataFactory.create(id = "2", title = "Napalm bombing during Vietnam War"),
                    SearchableVideoMetadataFactory.create(id = "3", title = "bombing during Vietnam War")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("Napalm bombing during Vietnam War"))
            )

            assertThat(results.first()).isEqualTo("2")
        }

        @Test
        fun `videos match via synonyms`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "Second world war")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("WW2"))
            )

            assertThat(results).containsExactly("1")
        }

        @Test
        fun `multiword synonyms must match exactly`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "video about ww2")
                )
            )

            assertThat(
                readSearchService.search(
                    PaginatedSearchRequest(query = VideoQuery("second world war"))
                )
            ).containsExactly("1")
            assertThat(
                readSearchService.search(
                    PaginatedSearchRequest(query = VideoQuery("second world"))
                )
            ).isEmpty()
        }

        @Test
        fun `multiword synonyms must match video metadata exactly`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "second world")
                )
            )

            assertThat(
                readSearchService.search(PaginatedSearchRequest(query = VideoQuery(phrase = "ww2")))
            ).isEmpty()
        }

        @Test
        fun `case sensitive synonyms`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "Welcome to the US"),
                    SearchableVideoMetadataFactory.create(id = "2", description = "Beware of us")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("United States of America"))
            )

            assertThat(results).containsExactly("1")
        }
    }

    @Nested
    inner class ContentPartnerSearches {
        @Test
        fun `content partner matches exactly and has no excluded tags`() {
            val contentProvider = "Bozeman Science"

            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        contentProvider = contentProvider,
                        tags = listOf("news")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        contentProvider = contentProvider,
                        tags = emptyList()
                    )
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            phrase = contentProvider,
                            excludeTags = listOf("news")
                        )
                    )
                )

            assertThat(results).containsExactly("2")
        }

        @Test
        fun `content partner matches exactly and has included tags`() {
            val contentProvider = "Bozeman Science"

            writeSearchService.upsert(
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

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = contentProvider,
                        includeTags = listOf("education")
                    )
                )
            )

            assertThat(results).containsExactly("1")
        }

        @Test
        fun `rank content partner matches above other field matches`() {
            writeSearchService.upsert(
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

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery(phrase = "Ted-ed"))
            )

            assertThat(results).startsWith("3")
        }

        @Test
        fun `can filter by duration bound`() {
            writeSearchService.upsert(
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
                        durationSeconds = 10,
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

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "TED",
                        minDuration = Duration.ofSeconds(5),
                        maxDuration = Duration.ofSeconds(10)
                    )
                )
            )

            assertThat(results).containsExactlyInAnyOrder("1", "2")
        }

        @Test
        fun `can filter by subject`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "0",
                        subjects = setOf("Maths"),
                        contentProvider = "TED"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        subjects = setOf("History"),
                        contentProvider = "TED"
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "TED",
                        subjects = setOf("History")
                    )
                )
            )

            assertThat(results).containsExactlyInAnyOrder("1")
        }
        @Test
        fun `can filter by age range`() {
            writeSearchService.upsert(
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

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "TED",
                        ageRangeMin = 2,
                        ageRangeMax = 5
                    )
                )
            )

            assertThat(results).containsExactlyInAnyOrder("0")
        }

    }

    @Nested
    inner class AgeRangeSearches {
        @Test
        fun `videos within query age range`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 15
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 7
                    ),
                    SearchableVideoMetadataFactory.create(id = "3", title = "TED", ageRangeMin = 7),
                    SearchableVideoMetadataFactory.create(id = "4", title = "TED", ageRangeMin = 3),
                    SearchableVideoMetadataFactory.create(
                        id = "5",
                        title = "TED",
                        ageRangeMin = 15,
                        ageRangeMax = 18
                    ),
                    SearchableVideoMetadataFactory.create(id = "6", title = "TED", ageRangeMin = 1, ageRangeMax = 3)
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            ageRangeMin = 5,
                            ageRangeMax = 11
                        )
                    )
                )

            assertThat(results).hasSize(4)
            assertThat(results).contains("1")
            assertThat(results).contains("2")
            assertThat(results).contains("3")
            assertThat(results).contains("4")
        }

        @Test
        fun `videos within query age range with only lower bound`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 15
                    ),
                    SearchableVideoMetadataFactory.create(id = "2", title = "TED", ageRangeMin = 7),
                    SearchableVideoMetadataFactory.create(id = "3", title = "TED", ageRangeMin = 3),
                    SearchableVideoMetadataFactory.create(id = "4", title = "TED", ageRangeMin = 1, ageRangeMax = 3)
                )
            )

            val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery(ageRangeMin = 5)))

            assertThat(results).hasSize(3)
            assertThat(results).contains("1")
            assertThat(results).contains("2")
            assertThat(results).contains("3")
        }

        @Test
        fun `videos within query age range with only upper bound`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 15
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        ageRangeMin = 7,
                        ageRangeMax = 11
                    ),
                    SearchableVideoMetadataFactory.create(id = "3", title = "TED", ageRangeMin = 3),
                    SearchableVideoMetadataFactory.create(id = "4", title = "TED", ageRangeMin = 13),
                    SearchableVideoMetadataFactory.create(
                        id = "5",
                        title = "TED",
                        ageRangeMin = 15,
                        ageRangeMax = 18
                    )
                )
            )

            val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery(ageRangeMax = 11)))

            assertThat(results).hasSize(3)
            assertThat(results).contains("1")
            assertThat(results).contains("2")
            assertThat(results).contains("3")
        }
    }

    @Nested
    inner class SubjectSearches {
        @Test
        fun `videos that match a given subject`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        subjects = setOf("my-fancy-subject")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        subjects = setOf("my-less-fancy-subject")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(subjects = setOf("my-fancy-subject"))
                )
            )

            assertThat(results).hasSize(1)
            assertThat(results).contains("1")
        }

        @Test
        fun `videos with any matching subject`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        subjects = setOf("subject-one")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        subjects = setOf("subject-two")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "TED",
                        subjects = setOf("subject-three")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(subjects = setOf("subject-one", "subject-two"))
                )
            )

            assertThat(results).hasSize(2)
            assertThat(results).contains("1")
            assertThat(results).contains("2")
        }

        @Test
        fun `videos tagged with multiple subjects`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        subjects = setOf("subject-one", "subject-two")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        subjects = setOf("subject-two", "subject-three")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "TED",
                        subjects = setOf("subject-three")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        subjects = setOf("subject-two")
                    )
                )
            )

            assertThat(results).hasSize(2)
            assertThat(results).contains("1")
            assertThat(results).contains("2")
        }

        @Test
        fun `does not match subjects`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        subjects = setOf("maths-123")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "",
                        subjects = setOf("biology-987")
                    )
                )
            )

            assertThat(results).hasSize(0)
        }
    }

    @Nested
    inner class Counting {
        @Test
        fun `counts search results for phrase queries`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "5", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "6", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "7", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "8", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "9", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "11", description = "candy banana apple")
                )
            )

            val results = readSearchService.count(VideoQuery(phrase = "banana"))

            assertThat(results).isEqualTo(11)
        }

        @Test
        fun `counts search results for IDs queries`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
                )
            )

            val results = readSearchService.count(VideoQuery(ids = listOf("2", "5")))

            assertThat(results).isEqualTo(1)
        }

        @Test
        fun `can count for just news results`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(
                        id = "4",
                        description = "candy banana apple",
                        tags = listOf("news")
                    )
                )
            )

            val results = readSearchService.count(VideoQuery(includeTags = listOf("news")))

            assertThat(results).isEqualTo(1)
        }
    }

    @Nested
    inner class Pagination {
        @Test
        fun `paginates search results`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            "banana"
                        ), startIndex = 0, windowSize = 2
                    )
                )

            assertThat(results.size).isEqualTo(2)
        }

        @Test
        fun `can retrieve any page`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
                )
            )

            val page1 = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "banana"
                    ), startIndex = 0, windowSize = 2
                )
            )
            val page2 = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "banana"
                    ), startIndex = 2, windowSize = 2
                )
            )
            val page3 = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "banana"
                    ), startIndex = 4, windowSize = 2
                )
            )

            assertThat(page1).doesNotContainAnyElementsOf(page2)
            assertThat(page1).hasSize(2)
            assertThat(page2).hasSize(2)
            assertThat(page3).hasSize(0)
        }
    }

    @Nested
    inner class IdSearches {
        @Test
        fun `returns exact matches for IDs search query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery(ids = listOf("2", "5")))
            )

            assertThat(results).containsExactly("2")
        }
    }

    @Nested
    inner class TagSearches {
        @Test
        fun `all include tags must match`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        description = "banana",
                        tags = listOf("classroom")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery(includeTags = listOf("classroom", "news")))
            )

            assertThat(results).isEmpty()
        }

        @Test
        fun `all exclude tags must match`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        description = "banana",
                        tags = listOf("classroom")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        excludeTags = listOf(
                            "classroom",
                            "news"
                        )
                    )
                )
            )

            assertThat(results).isEmpty()
        }

        @Test
        fun `having include and exclude as the same tag returns no results`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        description = "banana",
                        tags = listOf("classroom")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        excludeTags = listOf("classroom"),
                        includeTags = listOf("classroom")
                    )
                )
            )

            assertThat(results).isEmpty()
        }

        @Test
        fun `searching with no filters returns news and non-news`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "3", description = "banana"),
                    SearchableVideoMetadataFactory.create(
                        id = "9",
                        description = "candy banana apple",
                        tags = listOf("news")
                    ),
                    SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple")
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery(phrase = "banana"))
            )

            assertThat(results).hasSize(3)
        }
    }

    @Nested
    inner class DurationSearches {
        @Test
        fun `duration range matches`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                    SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 100),
                    SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 60)
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            minDuration = Duration.ofSeconds(60),
                            maxDuration = Duration.ofSeconds(110)
                        )
                    )
                )

            assertThat(results.size).isEqualTo(2)
            assertThat(results).containsExactlyInAnyOrder("2", "3")
        }

        @Test
        fun `duration range no upper bound`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                    SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 100),
                    SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 40)
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            minDuration = Duration.ofSeconds(60)
                        )
                    )
                )

            assertThat(results.size).isEqualTo(2)
            assertThat(results).containsExactlyInAnyOrder("1", "2")
        }

        @Test
        fun `duration range no lower bound`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                    SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 60),
                    SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 100)
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(maxDuration = Duration.ofSeconds(110))
                    )
                )

            assertThat(results.size).isEqualTo(2)
            assertThat(results).containsExactlyInAnyOrder("2", "3")
        }
    }

    @Nested
    inner class CombinationOfFiltersSearches {

        @Test
        fun `no filters return everything`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy")
                )
            )
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
                )
            )

            val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery()))

            assertThat(results).hasSize(2)
        }

        @Test
        fun `age, subject`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 15,
                        subjects = setOf("subject-1")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        ageRangeMin = 7,
                        subjects = setOf("subject-1")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 5,
                        subjects = setOf("subject-2")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        ageRangeMin = 3,
                        ageRangeMax = 5,
                        subjects = setOf("subject-1")
                    )
                )
            )

            assertThat(results).hasSize(1)
            assertThat(results).contains("1")
        }

        @Test
        fun `age, query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 5
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "Intercom Learning",
                        ageRangeMin = 3,
                        ageRangeMax = 5
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "Intercom Learning",
                        ageRangeMin = 3,
                        ageRangeMax = 5
                    )
                )
            )

            assertThat(results).hasSize(1)
            assertThat(results).containsExactly("2")
        }

        @Test
        fun `age, include tag`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        ageRangeMin = 3,
                        ageRangeMax = 5,
                        tags = listOf("classroom")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        tags = listOf("news"),
                        ageRangeMin = 3,
                        ageRangeMax = 5
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        ageRangeMin = 3,
                        ageRangeMax = 5,
                        includeTags = listOf("news")
                    )
                )
            )

            assertThat(results).hasSize(1)
            assertThat(results).containsExactly("3")
        }

        @Test
        fun `age, subject, query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        ageRangeMin = 3,
                        ageRangeMax = 15,
                        subjects = setOf("subject-1")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "TED",
                        ageRangeMin = 7,
                        subjects = setOf("subject-1")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "Intercom Learning",
                        ageRangeMin = 3,
                        ageRangeMax = 5,
                        subjects = setOf("subject-2")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "Intercom Learning",
                        ageRangeMin = 3,
                        ageRangeMax = 5,
                        subjects = setOf("subject-1")
                    )
                )
            )

            assertThat(results).isEmpty()
        }

        @Test
        fun `subject, query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        subjects = setOf("subject-one", "subject-two")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "HELLO",
                        subjects = setOf("subject-two", "subject-three")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "TED",
                        subjects = setOf("subject-two")
                    )
                )
            )

            assertThat(results).hasSize(1)
            assertThat(results).contains("1")
        }

        @Test
        fun `tags, subject`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "TED",
                        subjects = setOf("subject-one", "subject-two"),
                        tags = listOf("classroom")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "HELLO",
                        subjects = setOf("subject-two", "subject-three"),
                        tags = listOf("news")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        subjects = setOf("subject-two"),
                        includeTags = listOf("news")
                    )
                )
            )

            assertThat(results).hasSize(1)
            assertThat(results).contains("2")
        }

        @Test
        fun `include tag, query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                    SearchableVideoMetadataFactory.create(
                        id = "4",
                        description = "candy banana apple",
                        tags = listOf("news")
                    )
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            phrase = "banana",
                            includeTags = listOf("news")
                        )
                    )
                )

            assertThat(results).containsExactly("4")
        }

        @Test
        fun `exclude tags, query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "3", description = "some random banana isNews"),
                    SearchableVideoMetadataFactory.create(
                        id = "4",
                        description = "candy banana apple",
                        tags = listOf("news")
                    )
                )
            )

            val results =
                readSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            phrase = "banana",
                            excludeTags = listOf("news")
                        )
                    )
                )

            assertThat(results).containsExactly("3")
        }

        @Test
        fun `duration, query`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "0", durationSeconds = 50),
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        durationSeconds = 10,
                        title = "matching-query"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        durationSeconds = 50,
                        title = "matching-query"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        durationSeconds = 100,
                        title = "matching-query"
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "matching-query",
                        minDuration = Duration.ofSeconds(40),
                        maxDuration = Duration.ofSeconds(60)
                    )
                )
            )
            assertThat(results).containsExactly("2")
        }

        @Test
        fun `duration, subject`() {
            writeSearchService.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        durationSeconds = 10,
                        subjects = setOf("subject-two", "subject-three")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        durationSeconds = 50,
                        subjects = setOf("subject-two", "subject-three")
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        durationSeconds = 100,
                        subjects = setOf("subject-two", "subject-three")
                    )
                )
            )

            val results = readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        subjects = setOf("subject-two"),
                        minDuration = Duration.ofSeconds(49),
                        maxDuration = Duration.ofSeconds(51)
                    )
                )
            )
            assertThat(results).containsExactly("2")
        }
    }
}
