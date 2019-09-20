package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class CollectionDocumentConverter {
    fun convert(searchHit: SearchHit): CollectionDocument = ESObjectMapper.get()
        .readValue<CollectionDocument>(searchHit.sourceAsString)
        .also {
            if (it.hasAttachments == null) {
                return it.copy(hasAttachments = false)
            }
        }
}
