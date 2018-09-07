package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.Video
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class SearchHitConverter(private val objectMapper: ObjectMapper) {

    fun convert(searchHit: SearchHit): Video = objectMapper
            .readValue<ElasticSearchVideo>(searchHit.sourceAsString)
            .toVideo()
}