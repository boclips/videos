package com.boclips.search.service.infrastructure

import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class ElasticSearchResultConverter {

    fun convert(searchHit: SearchHit): ElasticSearchVideo = ElasticObjectMapper.get()
            .readValue(searchHit.sourceAsString)
}
