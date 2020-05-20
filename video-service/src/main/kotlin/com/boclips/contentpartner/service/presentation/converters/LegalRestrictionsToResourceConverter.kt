package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.presentation.hateoas.LegalRestrictionsLinkBuilder
import com.boclips.videos.api.response.channel.LegalRestrictionResource
import com.boclips.videos.api.response.channel.LegalRestrictionsResource
import com.boclips.videos.api.response.channel.LegalRestrictionsWrapper

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
