package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.presentation.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
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
