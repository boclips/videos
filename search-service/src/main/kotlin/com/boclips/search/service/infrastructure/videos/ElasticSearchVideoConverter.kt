package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.ElasticObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class ElasticSearchVideoConverter {

    fun convert(searchHit: SearchHit): ElasticSearchVideo = ElasticObjectMapper.get()
        .readValue(searchHit.sourceAsString)
}
