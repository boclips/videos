package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerStatus
import com.boclips.videos.api.response.contentpartner.ContentPartnerStatusWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentPartnerStatusesResource
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/marketing-statuses")
class MarketingStatusController(private val marketingStatusLinkBuilder: MarketingStatusLinkBuilder) {

    @GetMapping
    fun findAll() =
        marketingStatusLinkBuilder.self().let { self ->
            ContentPartnerStatusesResource(
                _embedded = ContentPartnerStatusWrapperResource(
                    statuses = ContentPartnerStatus.values().map {
                        it.name
                    }
                ),
                _links = mapOf(self.rel.value() to self)
            )
        }
}