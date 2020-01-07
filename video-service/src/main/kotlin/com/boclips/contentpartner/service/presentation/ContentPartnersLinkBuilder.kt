package com.boclips.contentpartner.service.presentation

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder

class ContentPartnersLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CONTENT_PARTNERS = "contentPartners"
    }

    fun self(id: String): Link {
        return ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartner(
                id
            )
        ).withSelfRel()
    }

    fun contentPartnerLink(id: String?): Link? {
        return getIfHasRole(UserRoles.VIEW_CONTENT_PARTNERS) {
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartner(
                    id
                )
            ).withRel("contentPartner")
        }
    }

    fun contentPartnersLink(): Link? {
        return getIfHasRole(UserRoles.VIEW_CONTENT_PARTNERS) {
            Link(
                getContentPartnersRoot()
                    .build()
                    .toUriString()
                    .plus("{?name,official,accreditedToYtChannelId}"),
                Rels.CONTENT_PARTNERS
            )
        }
    }

    private fun getContentPartnersRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/content-partners")
        .replaceQueryParams(null)
}
