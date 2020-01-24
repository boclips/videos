package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.WhatToExpectController
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class WhatToExpectLinkBuilder {
    fun whatToExpect(): Link ? = UserExtractor.getIfHasRole(UserRoles.VIEW_WHAT_TO_EXPECT){
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(WhatToExpectController::class.java).whatToExpect()
        ).withRel("whatToExpect")
    }
}