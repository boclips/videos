package com.boclips.videos.service.infrastructure.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.search.SearchHit

class ElasticSearchResultConverter(private val objectMapper: ObjectMapper) {

    fun convert(singleResult: GetResponse) = singleResult.sourceAsString?.let {
        objectMapper.readValue<ElasticSearchVideo>(it)
    }

    fun convert(searchHit: SearchHit): ElasticSearchVideo = objectMapper
            .readValue(searchHit.sourceAsString)
}