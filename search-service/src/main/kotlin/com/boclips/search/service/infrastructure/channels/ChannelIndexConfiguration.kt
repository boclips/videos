package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.infrastructure.IndexConfiguration

class ChannelIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {
        return mapOf(
            "properties" to mapOf(
                ChannelDocument.ID to IndexConfiguration.Fields.simpleText,
                ChannelDocument.NAME to IndexConfiguration.Fields.autocomplete,
                ChannelDocument.TYPES to IndexConfiguration.Fields.simpleTextArray,
                ChannelDocument.ELIGIBLE_FOR_STREAM to IndexConfiguration.Fields.boolean,
            )
        )
    }
}
