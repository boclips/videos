package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.IndexConfiguration

class VideoIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {
        val keywordField = mapOf(
            "type" to "keyword",
            "normalizer" to IndexConfiguration.Companion.Normalizers.LOWERCASE
        )
        val englishTextField = mapOf(
            "type" to "text",
            "analyzer" to IndexConfiguration.Companion.Analyzers.ENGLISH,
            "position_increment_gap" to 100
        )
        return mapOf(
            "properties" to mapOf(
                VideoDocument.TITLE to IndexConfiguration.Fields.freeText,
                VideoDocument.RAW_TITLE to keywordField,
                VideoDocument.DESCRIPTION to IndexConfiguration.Fields.freeText,
                VideoDocument.SUBJECT_IDS to IndexConfiguration.Fields.simpleTextArray,
                VideoDocument.SUBJECT_NAMES to IndexConfiguration.Fields.stringArray,
                VideoDocument.CONTENT_PROVIDER to keywordField,
                VideoDocument.RELEASE_DATE to IndexConfiguration.Fields.date,
                VideoDocument.TRANSCRIPT to IndexConfiguration.Fields.freeText,
                VideoDocument.TYPES to IndexConfiguration.Fields.simpleTextArray,
                VideoDocument.KEYWORDS to englishTextField,
                VideoDocument.PROMOTED to mapOf(
                    "type" to "boolean"
                ),
                VideoDocument.MEAN_RATING to IndexConfiguration.Fields.double,
                VideoDocument.SUBJECTS_SET_MANUALLY to IndexConfiguration.Fields.boolean,
                VideoDocument.ELIGIBLE_FOR_DOWNLOAD to IndexConfiguration.Fields.boolean,
                VideoDocument.ELIGIBLE_FOR_STREAM to IndexConfiguration.Fields.boolean,
                VideoDocument.TAGS to keywordField,
                VideoDocument.CONTENT_PARTNER_ID to IndexConfiguration.Fields.simpleText,
                VideoDocument.ATTACHMENT_TYPES to IndexConfiguration.Fields.simpleTextArray
            )
        )
    }
}
