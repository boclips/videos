package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.contentcategory.ContentCategoriesController
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class ContentCategoriesLinkBuilder {
    fun contentCategories(): Link? = getIfHasRole(UserRoles.VIEW_CONTENT_CATEGORIES) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ContentCategoriesController::class.java).contentCategories()
        ).withRel("contentCategories")
    }
}
