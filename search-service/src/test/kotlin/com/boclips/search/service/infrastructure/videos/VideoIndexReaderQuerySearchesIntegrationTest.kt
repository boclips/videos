package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderQuerySearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `matches on some words but not all`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "one two")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "one four"
                )
            )
        )

        assertThat(results.elements.first()).isEqualTo("1")
    }

    @Test
    fun `matches one field vs many`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "six two", description = "three four"),
                SearchableVideoMetadataFactory.create(id = "2", title = "six three", description = "two four")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "six two"
                )
            )
        )

        assertThat(results.elements).hasSize(2)
        assertThat(results.elements.first()).isEqualTo("1")
    }

    @Test
    fun `words appear in sequence in title increases score`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "Apple banana candy"
                )
            )
        )

        assertThat(results.elements.first()).isEqualTo("1")
    }

    @Test
    fun `words appear in sequence in description increases score`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "Apple banana candy"
                )
            )
        )

        assertThat(results.elements.first()).isEqualTo("1")
    }

    @Test
    fun `matches keywords`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "dogs"
                )
            )
        )

        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `content partners must match exactly`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Bozeman Science"),
                SearchableVideoMetadataFactory.create(id = "2", title = "a video about science")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "science"
                )
            )
        )

        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `transcripts match`() {
        videoIndexWriter.upsert(
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

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "thrones"))
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `title match is ranked higher than transcript match`() {
        videoIndexWriter.upsert(
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

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "thrones"))
        )

        assertThat(results.elements).hasSize(3)
        assertThat(results.elements).startsWith("2")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "i have a dream"))
        )

        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "rain"))
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `exact matches increase rank`() {
        videoIndexWriter.upsert(
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

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "Napalm bombing during Vietnam War"
                )
            )
        )

        assertThat(results.elements.first()).isEqualTo("2")
    }

    @Test
    fun `videos match via synonyms`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Second world war")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "WW2"))
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `multiword synonyms must match exactly`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "video about ww2")
            )
        )

        assertThat(
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(),
                        phrase = "second world war"
                    )
                )
            ).elements
        ).containsExactly("1")

        assertThat(
            videoIndexReader.search(
                PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "second world"))
            ).elements
        ).isEmpty()
    }

    @Test
    fun `multiword synonyms must match video metadata exactly`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "second world")
            )
        )

        assertThat(
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(),
                        phrase = "ww2"
                    )
                )
            ).elements
        ).isEmpty()
    }

    @Test
    fun `case sensitive synonyms`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Welcome to the US"),
                SearchableVideoMetadataFactory.create(id = "2", description = "Beware of us")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "United States of America"
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `literal match should score higher than stem match in title`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "empirical"),
                SearchableVideoMetadataFactory.create(id = "2", title = "empirical"),
                SearchableVideoMetadataFactory.create(id = "3", title = "empire"),
                SearchableVideoMetadataFactory.create(id = "4", title = "empir"),
                SearchableVideoMetadataFactory.create(id = "5", title = "empirical")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "empire"))
        )

        assertThat(results.elements).startsWith("3")
    }

    @Test
    fun `literal match should score higher than stem match in description`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "empirical"),
                SearchableVideoMetadataFactory.create(id = "2", description = "empirical"),
                SearchableVideoMetadataFactory.create(id = "3", description = "empire"),
                SearchableVideoMetadataFactory.create(id = "4", description = "empir"),
                SearchableVideoMetadataFactory.create(id = "5", description = "empirical")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "empire"))
        )

        assertThat(results.elements).startsWith("3")
    }

    @Test
    fun `phrase match in subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf(createSubjectMetadata(name = "art history"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    subjects = setOf(createSubjectMetadata(name = "history"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    subjects = setOf(createSubjectMetadata(name = "geography"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "history"))
        )

        assertThat(results.elements).containsExactly("2", "1")
    }

    @Test
    fun `phrase match in subject when multiple subjects`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf(createSubjectMetadata(name = "art history"), createSubjectMetadata(name = "sport"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "art history"))
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `phrase match in subject and other fields`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "history of mathematics",
                    subjects = setOf(createSubjectMetadata(name = "mathematics"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Roman Empire",
                    subjects = setOf(createSubjectMetadata(name = "history"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "history"))
        )

        assertThat(results.elements).containsExactly("2", "1")
    }

    @Test
    fun `phrase match partially in subject and partially in other fields`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf(createSubjectMetadata(name = "sport"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "fractions",
                    subjects = setOf(createSubjectMetadata(name = "mathematics"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "fractions",
                    subjects = setOf(createSubjectMetadata(name = "politics"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "mathematics fractions"
                )
            )
        )

        assertThat(results.elements).containsExactly("2", "3")
    }

    @Test
    fun `phrase match in subject via a synonym`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf(createSubjectMetadata(name = "mathematics"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "maths"))
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `instructional videos prioritised over news or stock`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "London Underground",
                    types = listOf(VideoType.NEWS)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "London Underground",
                    types = listOf(VideoType.NEWS)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "London Underground",
                    types = listOf(VideoType.INSTRUCTIONAL)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "London Underground",
                    types = listOf(VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "London Underground",
                    types = listOf(VideoType.STOCK)
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "London Underground"
                )
            )
        )

        assertThat(results.elements).startsWith("3")
        assertThat(results.elements).hasSize(5)
    }

    @Test
    fun `prioritise video title exact matching over other fields`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED-Ed: Tim Seibles: First Kiss",
                    description = """This animation is part of the TED-Ed series, "There's a Poem for That," which features animated interpretations of poems both old and new that give language to some of life's biggest feelings. [Poem by Tim Seibles, directed by Hannah Jacobs, music by Stephen LaRosa]."""
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2", title = "TED-ED: Why is glass transparent? - Mark Miodownik",
                    description = "If you look through your glasses, binoculars or a window, you see the world on the other side. How is it that something so solid can be so invisible? Mark Miodownik melts the scientific secret behind amorphous solids. Glasses",
                    transcript = "glasses lots of glasses"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED-Ed: How do glasses help us see? - Andrew Bastawrous and Clare Gilbert",
                    description = "Today, glasses help millions of people with poor vision be able to see clearly. But how? Andrew Bastawrous and Clare Gilbert help unravel the answer by explaining refraction - the ability of a transparent medium, like glass, water, or the eye, to change the direction of light passing through it."
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), phrase = "ted ed glasses"))
        )

        assertThat(results.elements).containsExactly("3", "2", "1")
        assertThat(results.elements).hasSize(3)
    }
}
