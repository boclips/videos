package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestrictions
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "legalRestrictions")
data class LegalRestrictionsResource(val id: String, val text: String) {

    companion object {
        fun from(legalRestrictions: LegalRestrictions): LegalRestrictionsResource {
            return LegalRestrictionsResource(
                id = legalRestrictions.id.value,
                text = legalRestrictions.text
            )
        }
    }
}
