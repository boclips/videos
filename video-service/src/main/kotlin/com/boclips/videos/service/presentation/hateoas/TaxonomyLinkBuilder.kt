package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.TaxonomyController
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class TaxonomyLinkBuilder {
    fun categories(): Link? = getIfHasRole(UserRoles.VIEW_TAXONOMIES) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(TaxonomyController::class.java).getCategories()
        ).withRel("categories")
    }
}
