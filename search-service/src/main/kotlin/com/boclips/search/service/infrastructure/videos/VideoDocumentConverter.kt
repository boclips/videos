package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class VideoDocumentConverter {
    fun convert(searchHit: SearchHit): VideoDocument = ESObjectMapper.get()
        .readValue(searchHit.sourceAsString)
}
