package com.boclips.videos.service.infrastructure.legal.restrictions

import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictions
import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictionsId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class LegalRestrictionsDocument(
    @BsonId val id: ObjectId,
    val text: String
) {

    fun toRestrictions(): LegalRestrictions {
        return LegalRestrictions(id = LegalRestrictionsId(id.toHexString()), text = text)
    }
}