package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.presentation.converters.ChannelToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContractsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.videos.service.presentation.converters.SuggestionToResourceConverter
import com.boclips.videos.service.presentation.hateoas.SuggestionLinkBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerPresentationContext")
class PresentationContext(
    private val uriComponentsBuilderFactory: UriComponentsBuilderFactory
) {

    @Bean
    fun channelLinkBuilder() =
        ChannelLinkBuilder(uriComponentsBuilderFactory = uriComponentsBuilderFactory)

    @Bean
    fun marketingStatusesLinkBuilder(): MarketingStatusLinkBuilder = MarketingStatusLinkBuilder()

    @Bean
    fun suggestionsLinkBuilder(): SuggestionLinkBuilder = SuggestionLinkBuilder(uriComponentsBuilderFactory)

    @Bean
    fun getChannelToResourceConverter(
        legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter,
        ingestDetailsToResourceConverter: IngestDetailsResourceConverter
    ): ChannelToResourceConverter {
        return ChannelToResourceConverter(
            channelLinkBuilder = channelLinkBuilder(),
            ingestDetailsToResourceConverter = ingestDetailsToResourceConverter,
            legalRestrictionsToResourceConverter = legalRestrictionsToResourceConverter
        )
    }

    @Bean
    fun getSuggestionConverter(): SuggestionToResourceConverter {
        return SuggestionToResourceConverter()
    }

    @Bean
    fun contractsLinkBuilder(
        uriComponentsBuilderFactory: UriComponentsBuilderFactory
    ): ContractsLinkBuilder {
        return ContractsLinkBuilder(uriComponentsBuilderFactory)
    }
}
