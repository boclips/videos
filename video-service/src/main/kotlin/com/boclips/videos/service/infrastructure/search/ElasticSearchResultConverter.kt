package com.boclips.videos.service.infrastructure.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class ElasticSearchResultConverter(private val objectMapper: ObjectMapper) {
    fun convert(searchHit: SearchHit): ElasticSearchVideo = objectMapper
            .readValue(searchHit.sourceAsString)
}