package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.search.service.domain.subjects.model.SubjectQuery
import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubjectsIndexIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: SubjectsIndexReader
    lateinit var indexWriter: SubjectsIndexWriter

    @BeforeEach
    fun setUp(){
        indexReader = SubjectsIndexReader(esClient)
        indexWriter = SubjectsIndexWriter(esClient, 20)
    }

    @Test
    fun `creates a new index and upserts the subject provided`(){
        indexWriter.safeRebuildIndex(
            sequenceOf(SearchableSubjectMetadataFactory.create(id = "1", name = "Super Subject"))
        )

        val results = indexReader.search(
            SearchRequestWithoutPagination(
                query = SubjectQuery(
                    "super"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("1")
    }
}