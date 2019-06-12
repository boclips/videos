package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.infrastructure.AbstractESWriteSearchService
import org.elasticsearch.client.RestHighLevelClient

class ESCollectionWriteSearchService(client: RestHighLevelClient) : AbstractESWriteSearchService<CollectionMetadata>(
    CollectionIndexConfiguration(),
    client,
    ESCollectionsIndex
) {

    override fun convertToSerializableObject(entry: CollectionMetadata) = ESCollection(
        id = entry.id,
        title = entry.title,
        subjects = entry.subjectIds
    )

    override fun getIdentifier(entry: CollectionMetadata) = entry.id

}
