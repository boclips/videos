package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class ChannelsDocumentConverter {
    fun convert(searchHit: SearchHit): ChannelDocument = ESObjectMapper.get()
        .readValue(searchHit.sourceAsString)

    fun convertToDocument(metadata: ChannelMetadata): ChannelDocument {
        return ChannelDocument(
            id = metadata.id,
            name = metadata.name,
            autocompleteName = metadata.name,
            eligibleForStream = metadata.eligibleForStream,
            types = metadata.contentTypes,
            ingestType = metadata.ingestType?.name,
            taxonomyVideoLevelTagging = metadata.taxonomy.videoLevelTagging,
            taxonomyCategories = metadata.taxonomy.categories?.map { it.value }?.sorted(),
            taxonomyCategoriesWithAncestors = metadata.taxonomy.categoriesWithAncestors?.map { it.value },
            isYoutube = metadata.isYoutube,
            isPrivate = metadata.isPrivate,
        )
    }
}
