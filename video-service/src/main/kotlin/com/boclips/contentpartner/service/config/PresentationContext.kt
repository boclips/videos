package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerPresentationContext")
class PresentationContext {
    @Bean
    fun contentPartnersLinkBuilder(uriComponentsBuilderFactory: UriComponentsBuilderFactory) =
        ContentPartnersLinkBuilder(
            uriComponentsBuilderFactory = uriComponentsBuilderFactory
        )

    @Bean
    fun marketingStatusesLinkBuilder(): MarketingStatusLinkBuilder =
        MarketingStatusLinkBuilder()
}
