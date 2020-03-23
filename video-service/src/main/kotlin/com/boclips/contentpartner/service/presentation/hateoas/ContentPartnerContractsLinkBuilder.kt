package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.ContentPartnerContractController
import com.boclips.videos.api.response.HateoasLink
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ContentPartnerContractsLinkBuilder {
    fun self(id: String): HateoasLink {
        val withSelfRel = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ContentPartnerContractController::class.java).getContentPartnerContract(
                id
            )
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }
}