package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.ContentPartnerContractController
import com.boclips.contentpartner.service.presentation.UriComponentsBuilderFactory
import com.boclips.security.utils.UserExtractor
import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ContentPartnerContractsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CONTENT_PARTNER_CONTRACT = "contentPartnerContract"
        const val CONTENT_PARTNER_CONTRACTS = "contentPartnerContracts"
    }

    fun self(id: String): HateoasLink {
        val withSelfRel = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ContentPartnerContractController::class.java).getContentPartnerContract(
                id
            )
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }

    fun contentPartnerContractLink(id: String?): Link? {
        return getIfHasRole(UserRoles.VIEW_CONTENT_PARTNER_CONTRACTS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ContentPartnerContractController::class.java).getContentPartnerContract(
                    id
                )
            ).withRel(Rels.CONTENT_PARTNER_CONTRACT)
        }
    }

    fun contentPartnerContractsLink(): Link? {
        return getIfHasRole(
            UserRoles.INSERT_CONTENT_PARTNER_CONTRACTS
        ) {
            Link(
                getContentPartnerContractsRoot()
                    .build()
                    .toUriString(),
                Rels.CONTENT_PARTNER_CONTRACTS
            )
        }
    }

    private fun getContentPartnerContractsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/content-partner-contracts")
        .replaceQueryParams(null)
}