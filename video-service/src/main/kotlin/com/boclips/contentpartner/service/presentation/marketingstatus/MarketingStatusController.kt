package com.boclips.contentpartner.service.presentation.marketingstatus

import com.boclips.contentpartner.service.domain.model.channel.ChannelStatus
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import com.boclips.videos.api.response.channel.StatusWrapperResource
import com.boclips.videos.api.response.channel.StatusesResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/marketing-statuses")
class MarketingStatusController(private val marketingStatusLinkBuilder: MarketingStatusLinkBuilder) {

    @GetMapping
    fun findAll() =
        marketingStatusLinkBuilder.self().let { self ->
            StatusesResource(
                _embedded = StatusWrapperResource(
                    statuses = ChannelStatus.values().map {
                        it.name
                    }
                ),
                _links = mapOf(self.rel.value() to self)
            )
        }
}
