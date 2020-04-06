package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.newlegalrestriction.NewLegalRestrictionsController
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn

class NewLegalRestrictionsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun self(type: String): HateoasLink {
        val withSelfRel = linkTo(
            methodOn(NewLegalRestrictionsController::class.java).getOne(type)
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }

    fun newLegalRestrictionLink(id: String?): Link? {
        return UserExtractor.getIfHasRole(UserRoles.VIEW_LEGAL_RESTRICTIONS) {
            linkTo(
                methodOn(NewLegalRestrictionsController::class.java).getOne(id)
            ).withRel("legalRestriction")
        }
    }

    fun newLegalRestrictionsLink(): Link? {
        return UserExtractor.getIfHasRole(UserRoles.VIEW_LEGAL_RESTRICTIONS) {
            linkTo(
                methodOn(NewLegalRestrictionsController::class.java).getAll()
            ).withRel("legalRestrictions")
        }
    }

    fun createNewLegalRestrictionLink(): Link? {
        return UserExtractor.getIfHasRole(UserRoles.CREATE_LEGAL_RESTRICTIONS) {
            linkTo(
                getNewLegalRestrictionsRoot().build().toUriString()
            ).withRel("CreateNewLegalRestrictions")
        }
    }

    private fun getNewLegalRestrictionsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/new-legal-restrictions")
        .replaceQueryParams(null)
}