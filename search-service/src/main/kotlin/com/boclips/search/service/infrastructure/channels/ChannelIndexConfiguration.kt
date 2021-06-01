package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.infrastructure.IndexConfiguration
import com.boclips.search.service.infrastructure.IndexConfiguration.Fields

class ChannelIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {

        return mapOf(
            "properties" to mapOf(
                ChannelDocument.ID to Fields.simpleText,
                ChannelDocument.NAME to Fields.caseInsensitiveSimpleText,
                ChannelDocument.AUTOCOMPLETE_NAME to Fields.autocomplete,
                ChannelDocument.TYPES to Fields.simpleTextArray,
                ChannelDocument.INGEST_TYPE to Fields.simpleText,
                ChannelDocument.ELIGIBLE_FOR_STREAM to Fields.boolean,
                ChannelDocument.TAXONOMY_VIDEO_LEVEL_TAGGING to Fields.boolean,
                ChannelDocument.TAXONOMY_CATEGORIES to Fields.simpleText,
            )
        )
    }
}
