package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.Video
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

object SearchHitConverter {

    private val objectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun convert(searchHit: SearchHit): Video = objectMapper.readValue<ElasticSearchVideo>(searchHit.sourceAsString).toVideo()
}