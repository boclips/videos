package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class ContentPartnersLinkBuilder {

    fun self(contentPartner: ContentPartner): Link {
        return ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartner(
                contentPartner.contentPartnerId.value
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