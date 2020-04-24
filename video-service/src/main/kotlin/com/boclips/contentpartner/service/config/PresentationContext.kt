package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.presentation.converters.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.videos.service.presentation.converters.SuggestionToResourceConverter
import com.boclips.videos.service.presentation.hateoas.SuggestionLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerPresentationContext")
class PresentationContext(
    private val uriComponentsBuilderFactory: UriComponentsBuilderFactory
) {

    @Bean
    fun contentPartnersLinkBuilder() =
        ContentPartnersLinkBuilder(uriComponentsBuilderFactory = uriComponentsBuilderFactory)

    @Bean
    fun marketingStatusesLinkBuilder(): MarketingStatusLinkBuilder = MarketingStatusLinkBuilder()

    @Bean
    fun suggestionsLinkBuilder(): SuggestionLinkBuilder = SuggestionLinkBuilder(uriComponentsBuilderFactory)

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

    @Bean
    fun getSuggestionConverter(videosLinkBuilder: VideosLinkBuilder): SuggestionToResourceConverter {
        return SuggestionToResourceConverter(videosLinkBuilder)
    }
}
