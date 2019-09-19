package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.LegalRestrictions
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class LegalRestrictionsDocument(
    @BsonId val id: ObjectId,
    val text: String
) {

    fun toRestrictions(): LegalRestrictions {
        return LegalRestrictions(
            id = LegalRestrictionsId(
                id.toHexString()
            ), text = text
        )
    }

    companion object {

        fun from(legalRestrictions: LegalRestrictions): LegalRestrictionsDocument {
            return LegalRestrictionsDocument(
                ObjectId(
                    legalRestrictions.id.value
                ), legalRestrictions.text
            )
        }
    }
}
