package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class ESCollectionConverter {

    fun convert(searchHit: SearchHit): ESCollection = ESObjectMapper.get()
        .readValue(searchHit.sourceAsString)
}
