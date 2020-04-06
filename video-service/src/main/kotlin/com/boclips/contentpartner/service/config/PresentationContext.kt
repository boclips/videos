package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.presentation.converters.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerPresentationContext")
class PresentationContext(
    private val uriComponentsBuilderFactory: UriComponentsBuilderFactory
) {

    @Bean
    fun contentPartnersLinkBuilder() =
        ContentPartnersLinkBuilder(
            uriComponentsBuilderFactory = uriComponentsBuilderFactory
        )

    @Bean
    fun marketingStatusesLinkBuilder(): MarketingStatusLinkBuilder = MarketingStatusLinkBuilder()

    @Bean
    fun getContentPartnerToResourceConverter(
        legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter,
        ingestDetailsToResourceConverter: IngestDetailsResourceConverter
    ): ContentPartnerToResourceConverter {
        return ContentPartnerToResourceConverter(
            contentPartnersLinkBuilder = contentPartnersLinkBuilder(),
            ingestDetailsToResourceConverter = ingestDetailsToResourceConverter,
            legalRestrictionsToResourceConverter = legalRestrictionsToResourceConverter
        )
    }
}
