package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.IndexConfiguration

class VideoIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {
        val keywordField = mapOf(
            "type" to "keyword",
            "normalizer" to IndexConfiguration.Companion.Normalizers.LOWERCASE
        )
        val englishKeywordField = mapOf(
            "type" to "text",
            "analyzer" to IndexConfiguration.Companion.Analyzers.ENGLISH,
            "position_increment_gap" to 100
        )
        return mapOf(
            "properties" to mapOf(
                VideoDocument.TITLE to IndexConfiguration.Fields.freeText,
                VideoDocument.DESCRIPTION to IndexConfiguration.Fields.freeText,
                VideoDocument.SUBJECT_IDS to IndexConfiguration.Fields.stringArray,
                VideoDocument.SUBJECT_NAMES to IndexConfiguration.Fields.stringArray,
                VideoDocument.CONTENT_PROVIDER to keywordField,
                VideoDocument.RELEASE_DATE to IndexConfiguration.Fields.date,
                VideoDocument.TRANSCRIPT to IndexConfiguration.Fields.freeText,
                VideoDocument.TYPE to keywordField,
                VideoDocument.KEYWORDS to englishKeywordField
            )
        )
    }
}
