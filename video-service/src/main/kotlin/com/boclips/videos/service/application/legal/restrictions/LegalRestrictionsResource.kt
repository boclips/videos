package com.boclips.videos.service.application.legal.restrictions

import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictions
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "legalRestrictions")
data class LegalRestrictionsResource(val id: String, val text: String) {

    companion object {
        fun from(legalRestrictions: LegalRestrictions): LegalRestrictionsResource {
            return LegalRestrictionsResource(id = legalRestrictions.id.value, text = legalRestrictions.text)
        }
    }
}