package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.legalrestriction.LegalRestrictionsController
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class LegalRestrictionsLinkBuilder {
    fun createLink(): Link? {
        return UserExtractor.getIfHasRole(UserRoles.UPDATE_VIDEOS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(LegalRestrictionsController::class.java).post(
                    null
                )
            ).withRel("create")
        }
    }

    fun getAllLink(): Link? {
        return UserExtractor.getIfHasRole(UserRoles.UPDATE_VIDEOS) {
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LegalRestrictionsController::class.java).getAll())
                .withRel("legalRestrictions")
        }
    }

    fun self(id: String): Link {
        return WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(LegalRestrictionsController::class.java).getOne(id)
        ).withSelfRel()
    }
}
