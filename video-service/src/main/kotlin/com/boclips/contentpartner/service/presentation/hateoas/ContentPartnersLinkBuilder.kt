package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.ContentPartnerController
import com.boclips.contentpartner.service.presentation.UriComponentsBuilderFactory
import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ContentPartnersLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CONTENT_PARTNERS = "contentPartners"
    }

    fun self(id: String): HateoasLink {
        val withSelfRel = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartner(
                id
            )
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }

    fun contentPartnerLink(id: String?): Link? {
        return getIfHasRole(UserRoles.VIEW_CONTENT_PARTNERS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartner(
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

    fun contentPartnersSignedUploadLink(): Link? =
        getIfHasAnyRole(UserRoles.INSERT_CONTENT_PARTNERS, UserRoles.UPDATE_CONTENT_PARTNERS) {
            Link(
                getContentPartnersRoot()
                    .build()
                    .toUriString()
                    .plus("/signed-upload-link"),
                "contentPartnersSignedUploadLink"
            )
        }

    private fun getContentPartnersRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/content-partners")
        .replaceQueryParams(null)
}
