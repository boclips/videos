package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.IndexConfiguration
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.ATTACHMENT_TYPES
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.BOOKMARKED_BY
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.HAS_ATTACHMENTS
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.LAST_MODIFIED
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.OWNER
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.SUBJECTS
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.TITLE
import com.boclips.search.service.infrastructure.collections.CollectionDocument.Companion.VISIBILITY

class CollectionIndexConfiguration : IndexConfiguration {
    override fun generateMapping(): Map<String, Any> {
        return mapOf(
            "properties" to mapOf(
                TITLE to IndexConfiguration.Fields.freeTextSortable,
                OWNER to IndexConfiguration.Fields.simpleText,
                BOOKMARKED_BY to IndexConfiguration.Fields.simpleTextArray,
                VISIBILITY to IndexConfiguration.Fields.simpleText,
                SUBJECTS to IndexConfiguration.Fields.stringArray,
                HAS_ATTACHMENTS to IndexConfiguration.Fields.boolean,
                LAST_MODIFIED to IndexConfiguration.Fields.date,
                ATTACHMENT_TYPES to IndexConfiguration.Fields.simpleTextArray
            )
        )
    }
}
