package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.contract.legalrestriction.ContractLegalRestrictionsController
import com.boclips.videos.api.response.HateoasLink
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn

class ContractLegalRestrictionsLinkBuilder() {
    fun contractLegalRestrictions(): HateoasLink {
        val link = linkTo(
            methodOn(ContractLegalRestrictionsController::class.java).getAll()
        ).withRel("contractLegalRestrictions")


        return HateoasLink(href = link.href, rel = link.rel.value())
    }
}