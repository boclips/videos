package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.IndexConfiguration

class CollectionIndexConfiguration : IndexConfiguration {

    override fun generateMapping(): Map<String, Any> {
        return mapOf(
            "properties" to mapOf(
                "title" to IndexConfiguration.Fields.freeText,
                "subjects" to IndexConfiguration.Fields.stringArray
            )
        )
    }
}
