package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import org.elasticsearch.client.RestHighLevelClient

class CollectionIndexWriter(client: RestHighLevelClient) : AbstractIndexWriter<CollectionMetadata>(
    indexConfiguration = CollectionIndexConfiguration(),
    client = client,
    esIndex = CollectionsIndex
) {
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
        bookmarkedBy = entry.bookmarkedByUsers
    )

    override fun getIdentifier(entry: CollectionMetadata) = entry.id
}
