package com.boclips.videos.service.presentation.hateoas

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
}
