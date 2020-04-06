package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.contract.ContentPartnerContractController
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ContentPartnerContractsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CONTENT_PARTNER_CONTRACT = "contentPartnerContract"
        const val CREATE_CONTENT_PARTNER_CONTRACTS = "createContentPartnerContracts"
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
        return getIfHasRole(UserRoles.VIEW_CONTENT_PARTNER_CONTRACTS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ContentPartnerContractController::class.java).getAll(
                    null, null
                )
            ).withRel(Rels.CONTENT_PARTNER_CONTRACTS)
        }
    }

    fun createContractLink(): Link? {
        return getIfHasRole(
            UserRoles.INSERT_CONTENT_PARTNER_CONTRACTS
        ) {
            Link(
                getContentPartnerContractsRoot()
                    .build()
                    .toUriString(),
                Rels.CREATE_CONTENT_PARTNER_CONTRACTS
            )
        }
    }

    fun createSignedUploadLink(): Link? =
        getIfHasRole(UserRoles.INSERT_CONTENT_PARTNER_CONTRACTS) {
            Link(
                getContentPartnerContractsRoot()
                    .build()
                    .toUriString()
                    .plus("/signed-upload-link"),
                "createContentPartnerContractsSignedUploadLink"
            )
        }

    private fun getContentPartnerContractsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/content-partner-contracts")
        .replaceQueryParams(null)
}
