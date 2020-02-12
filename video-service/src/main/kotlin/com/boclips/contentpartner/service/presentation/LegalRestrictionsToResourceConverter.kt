package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.presentation.hateoas.LegalRestrictionsLinkBuilder
import com.boclips.videos.api.response.contentpartner.LegalRestrictionResource
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsWrapper

class LegalRestrictionsToResourceConverter(private val legalRestrictionsLinkBuilder: LegalRestrictionsLinkBuilder) {
    fun convert(legalRestriction: LegalRestriction): LegalRestrictionResource {
        return LegalRestrictionResource(
            id = legalRestriction.id.value,
            text = legalRestriction.text,
            _links = listOfNotNull(legalRestrictionsLinkBuilder.self(legalRestriction.id.value))
                .map { it.rel.value() to it }
                .toMap()
        )
    }

    fun convert(legalRestriction: List<LegalRestriction>): LegalRestrictionsResource {
        val legalRestrictionsResources = legalRestriction.map { convert(it) }

        return LegalRestrictionsResource(
            _embedded = LegalRestrictionsWrapper(legalRestrictions = legalRestrictionsResources),
            _links = listOfNotNull(legalRestrictionsLinkBuilder.createLink()).map { it.rel.value() to it }.toMap()
        )
    }
}
