package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.marketingstatus.MarketingStatusController
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class MarketingStatusLinkBuilder {
    fun marketingStatuses(): Link? = UserExtractor.getIfHasRole(UserRoles.VIEW_MARKETING_STATUSES) {
        linkToMarketingStatuses("marketingStatuses")
    }

    fun self(): Link = linkToMarketingStatuses("self")

    private fun linkToMarketingStatuses(rel: String) =
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(MarketingStatusController::class.java).findAll()
        ).withRel(rel)
}
