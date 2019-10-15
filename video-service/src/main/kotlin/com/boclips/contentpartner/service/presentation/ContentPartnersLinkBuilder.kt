package com.boclips.contentpartner.service.presentation

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class ContentPartnersLinkBuilder {

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
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartners(
                    name = null,
                    official = null,
                    accreditedToYtChannelId = null
                )
            ).withRel("contentPartners")
        }
    }
}
