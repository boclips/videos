package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.presentation.ContentPartnerController
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
                ControllerLinkBuilder.methodOn(ContentPartnerController::class.java).getContentPartners()
            ).withRel("contentPartners")
        }
    }
}
