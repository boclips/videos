package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.IndexConfiguration
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.HAS_ATTACHMENTS
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.OWNER
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.SUBJECTS
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.TITLE
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.VISIBILITY

class CollectionIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {
        return mapOf(
            "properties" to mapOf(
                TITLE to IndexConfiguration.Fields.freeText,
                OWNER to IndexConfiguration.Fields.simpleText,
                VISIBILITY to IndexConfiguration.Fields.simpleText,
                SUBJECTS to IndexConfiguration.Fields.stringArray,
                HAS_ATTACHMENTS to IndexConfiguration.Fields.boolean
            )
        )
    }
}
