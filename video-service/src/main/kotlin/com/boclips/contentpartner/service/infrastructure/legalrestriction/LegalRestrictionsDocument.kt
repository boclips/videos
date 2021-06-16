package com.boclips.contentpartner.service.infrastructure.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class LegalRestrictionsDocument(
    @BsonId val id: ObjectId,
    val text: String
) {

    fun toRestrictions(): LegalRestriction {
        return LegalRestriction(
            id = LegalRestrictionsId(
                id.toHexString()
            ),
            text = text
        )
    }

    companion object {

        fun from(legalRestriction: LegalRestriction): LegalRestrictionsDocument {
            return LegalRestrictionsDocument(
                ObjectId(
                    legalRestriction.id.value
                ),
                legalRestriction.text
            )
        }
    }
}
