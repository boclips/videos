package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.IndexConfiguration

class VideoIndexConfiguration : IndexConfiguration {

    override fun generateMapping(): Map<String, Any> {
        val contentPartnerField = mapOf(
            "type" to "keyword",
            "normalizer" to IndexConfiguration.Companion.Normalizers.LOWERCASE
        )
        val keywordField = mapOf(
            "type" to "text",
            "analyzer" to IndexConfiguration.Companion.Analyzers.ENGLISH,
            "position_increment_gap" to 100
        )
        return mapOf(
            "properties" to mapOf(
                "title" to IndexConfiguration.Fields.freeText,
                "description" to IndexConfiguration.Fields.freeText,
                "contentProvider" to contentPartnerField,
                "releaseDate" to IndexConfiguration.Fields.date,
                "transcript" to IndexConfiguration.Fields.freeText,
                "keywords" to keywordField
            )
        )
    }
}