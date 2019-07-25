package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.infrastructure.video.mongo.UserTagDocument
import org.bson.types.ObjectId

object UserTagDocumentConverter {

    fun toDocument(tag: UserTag): UserTagDocument {
        return tag.let {
            UserTagDocument(
                id = ObjectId(it.tag.id.value),
                label = it.tag.label,
                userId = it.userId.value
            )
        }
    }

    fun toTag(userTagDocument: UserTagDocument): UserTag {
        return userTagDocument.let {
            UserTag(
                tag = Tag(
                    id = TagId(it.id.toHexString()),
                    label = it.label
                ),
                userId = UserId(it.userId)
            )
        }
    }
}
