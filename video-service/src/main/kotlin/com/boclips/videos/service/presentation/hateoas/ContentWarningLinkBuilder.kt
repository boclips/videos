package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.ContentWarningController
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ContentWarningLinkBuilder {
    fun createLink(): Link? {
        return UserExtractor.getIfHasRole(UserRoles.CREATE_CONTENT_WARNINGS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ContentWarningController::class.java).create(
                    null
                )
            ).withRel("create")
        }
    }

    fun getAllLink(): Link? {
        return UserExtractor.getIfHasRole(UserRoles.VIEW_CONTENT_WARNINGS) {
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentWarningController::class.java).getAll())
                .withRel("contentWarnings")
        }
    }

    fun self(id: String): Link {
        return WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ContentWarningController::class.java).get(id)
        ).withSelfRel()
    }
}
