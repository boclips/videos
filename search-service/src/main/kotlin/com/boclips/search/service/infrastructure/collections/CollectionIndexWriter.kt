package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class CollectionIndexWriter private constructor(client: RestHighLevelClient, indexParameters: IndexParameters) : AbstractIndexWriter<CollectionMetadata>(
    indexConfiguration = CollectionIndexConfiguration(),
    indexParameters = indexParameters,
    client = client,
    esIndex = CollectionsIndex
) {
    companion object {
        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters
        ): CollectionIndexWriter {
            return CollectionIndexWriter(client, indexParameters)
        }
        fun createTestInstance(client: RestHighLevelClient): CollectionIndexWriter {
            return CollectionIndexWriter(client, IndexParameters(numberOfShards = 1))
        }
    }

    override fun serializeToIndexDocument(entry: CollectionMetadata) = CollectionDocument(
        id = entry.id,
        title = entry.title,
        visibility = when(entry.visibility) {
            CollectionVisibility.PUBLIC -> "public"
            CollectionVisibility.PRIVATE -> "private"
        },
        subjects = entry.subjectIds,
        hasAttachments = entry.hasAttachments,
        owner = entry.owner,
        bookmarkedBy = entry.bookmarkedByUsers,
        description = entry.description,
        hasLessonPlans = entry.hasLessonPlans
    )

    override fun getIdentifier(entry: CollectionMetadata) = entry.id
}
