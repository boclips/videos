package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.infrastructure.IndexConfiguration

class SubjectIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {
        return mapOf(
            "properties" to mapOf(
                SubjectDocument.ID to IndexConfiguration.Fields.simpleText,
                SubjectDocument.NAME to IndexConfiguration.Fields.autocomplete,
            )
        )
    }
}
