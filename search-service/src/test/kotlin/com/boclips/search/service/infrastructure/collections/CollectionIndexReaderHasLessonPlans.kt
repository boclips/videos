package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderHasLessonPlans : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `can retrieve collections with lesson plans`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    hasLessonPlans = true
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Girl Dancing",
                    hasLessonPlans = false
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Beautiful Boy Dancing",
                    hasLessonPlans = null
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        phrase = "Beautiful",
                        has_lesson_plans = true
                    )
                )
            )

        Assertions.assertThat(results).containsExactly("1")
    }
}