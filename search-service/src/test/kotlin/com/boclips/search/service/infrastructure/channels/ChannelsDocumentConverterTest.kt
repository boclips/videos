package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.channels.model.IngestType
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions.assertThat
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
                "name": "this is channel name",
                "autocompleteName": "this is channel name",
                "eligibleForStream": true,
                "taxonomyVideoLevelTagging": true,
                "taxonomyCategories": null,
                "types": ["NEWS", "STOCK", "INSTRUCTIONAL"],
                "ingestType": "MANUAL"
            }
                """.trimIndent()
            )
        )

        val actualDocument = elasticSearchResultConverter.convert(searchHit)

        val name = "this is channel name"
        val expectedDocument = ChannelDocument(
            id = "14",
            name = name,
            autocompleteName = name,
            eligibleForStream = true,
            types = listOf(ContentType.NEWS, ContentType.STOCK, ContentType.INSTRUCTIONAL),
            taxonomyVideoLevelTagging = true,
            taxonomyCategories = null,
            taxonomyCategoriesWithAncestors = null,
            ingestType = IngestType.MANUAL.name,
            isYoutube = null,
            isPrivate = false
        )

        assertThat(actualDocument).isEqualTo(expectedDocument)
    }

    @Test
    fun `convert metadata to document`() {
        val metadata = SearchableChannelMetadataFactory.create(
            id = "14",
            name = "The title",
            eligibleForStream = true,
            contentTypes = listOf(ContentType.NEWS, ContentType.STOCK, ContentType.INSTRUCTIONAL),
            ingestType = IngestType.MRSS,
            taxonomy = Taxonomy(videoLevelTagging = true),
            isYoutube = true,
            isPrivate = true,
        )

        val document = ChannelsDocumentConverter().convertToDocument(metadata)

        val name = "The title"
        assertThat(document).isEqualTo(
            ChannelDocument(
                id = "14",
                name = name,
                autocompleteName = name,
                eligibleForStream = true,
                types = listOf(ContentType.NEWS, ContentType.STOCK, ContentType.INSTRUCTIONAL),
                ingestType = "MRSS",
                taxonomyVideoLevelTagging = true,
                taxonomyCategories = null,
                taxonomyCategoriesWithAncestors = null,
                isYoutube = true,
                isPrivate = true,
            )
        )
    }

    @Test
    fun `when converting to document, sort taxonomy categories`() {
        val metadata = SearchableChannelMetadataFactory.create(
            id = "14",
            name = "The title",
            eligibleForStream = true,
            ingestType = IngestType.MRSS,
            contentTypes = listOf(ContentType.NEWS, ContentType.STOCK, ContentType.INSTRUCTIONAL),
            taxonomy = Taxonomy(
                categories = setOf(CategoryCode("DE"), CategoryCode("AB"), CategoryCode("D")),
                categoriesWithAncestors = null,
                videoLevelTagging = false
            ),
            isYoutube = false
        )

        val document = ChannelsDocumentConverter().convertToDocument(metadata)

        val name = "The title"
        assertThat(document).isEqualTo(
            ChannelDocument(
                id = "14",
                name = name,
                autocompleteName = name,
                eligibleForStream = true,
                types = listOf(ContentType.NEWS, ContentType.STOCK, ContentType.INSTRUCTIONAL),
                ingestType = "MRSS",
                taxonomyVideoLevelTagging = false,
                taxonomyCategories = listOf("AB", "D", "DE"),
                taxonomyCategoriesWithAncestors = null,
                isYoutube = false,
            )
        )
    }

    @Test
    fun `when converting to document, remove duplicates from taxonomyWithDocuments`() {
        val metadata = SearchableChannelMetadataFactory.create(
            id = "14",
            name = "The title",
            eligibleForStream = true,
            ingestType = IngestType.MRSS,
            contentTypes = listOf(ContentType.NEWS, ContentType.STOCK, ContentType.INSTRUCTIONAL),
            taxonomy = Taxonomy(
                categories = setOf(CategoryCode("DE"), CategoryCode("AB"), CategoryCode("D")),
                categoriesWithAncestors = setOf(
                    CategoryCode("DE"),
                    CategoryCode("D"),
                    CategoryCode("A"),
                    CategoryCode("AB"),
                    CategoryCode("D")
                ),
                videoLevelTagging = false
            ),
            isYoutube = false
        )

        val document = ChannelsDocumentConverter().convertToDocument(metadata)
        assertThat(document.taxonomyCategoriesWithAncestors).containsExactlyInAnyOrder("A", "AB", "DE", "D")
    }

    @Test
    fun `can convert a search hit with missing ingest types`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "name": "this is channel name",
                "autocompleteName": "this is channel name",
                "eligibleForStream": true,
                "taxonomyVideoLevelTagging": true,
                "taxonomyCategories": null,
                "types": ["NEWS", "STOCK", "INSTRUCTIONAL"]
            }
                """.trimIndent()
            )
        )

        val actualDocument = elasticSearchResultConverter.convert(searchHit)

        assertThat(actualDocument.ingestType).isNull()
    }
}
