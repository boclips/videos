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
                VideoDocument.TITLE to IndexConfiguration.Fields.freeText,
                VideoDocument.DESCRIPTION to IndexConfiguration.Fields.freeText,
                VideoDocument.SUBJECTS to IndexConfiguration.Fields.stringArray,
                VideoDocument.CONTENT_PROVIDER to contentPartnerField,
                VideoDocument.RELEASE_DATE to IndexConfiguration.Fields.date,
                VideoDocument.TRANSCRIPT to IndexConfiguration.Fields.freeText,
                VideoDocument.KEYWORDS to keywordField
            )
        )
    }
}
