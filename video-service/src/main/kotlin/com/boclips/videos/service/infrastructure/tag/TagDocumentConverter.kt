package com.boclips.videos.service.infrastructure.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import org.bson.types.ObjectId

object TagDocumentConverter {

    fun toTag(document: TagDocument): Tag {
        return Tag(
            id = TagId(document.id.toHexString()),
            name = document.name
        )
    }

    fun toTagDocument(tag: Tag): TagDocument {
        return TagDocument(
            id = ObjectId(tag.id.value),
            name = tag.name
        )
    }
}
