package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions.*
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test


internal class ChannelsDocumentConverterTest {
    private val elasticSearchResultConverter = ChannelsDocumentConverter()

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "name": "this is channel name"
            }
        """.trimIndent()
            )
        )

        val actualDocument = elasticSearchResultConverter.convert(searchHit)

        val expectedDocument = ChannelDocument(
            id = "14",
            name = "this is channel name",
        )

        assertThat(actualDocument).isEqualTo(expectedDocument)
    }

    @Test
    fun `convert metadata to document`() {
        val metadata = SearchableChannelMetadataFactory.create(
            id = "14",
            name = "The title",
        )

        val document = ChannelsDocumentConverter().convertToDocument(metadata)

        assertThat(document).isEqualTo(
            ChannelDocument(
                id = "14",
                name = "The title",
            )
        )
    }
}