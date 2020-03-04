package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.application.ContentPartnerUpdatesConverter
import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.CreateAgeRange
import com.boclips.contentpartner.service.application.AgeRangeResourceConverter
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.application.GetAgeRange
import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.GcsSignedLinkProvider
import com.boclips.contentpartner.service.presentation.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.AgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.IngestDetailsToResourceConverter
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerApplicationContext")
class ApplicationContext(
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val contentPartnerRepository: ContentPartnerRepository,
    val ageRangeRepository: AgeRangeRepository
) {
    @Bean
    fun getContentPartner(): GetContentPartner {
        return GetContentPartner(contentPartnerRepository)
    }

    @Bean
    fun getContentPartners(): GetContentPartners {
        return GetContentPartners(contentPartnerRepository)
    }

    @Bean
    fun createContentPartner(): CreateContentPartner {
        return CreateContentPartner(contentPartnerRepository, ageRangeRepository)
    }

    @Bean
    fun createAgeRange(): CreateAgeRange {
        return CreateAgeRange(ageRangeRepository)
    }

    @Bean
    fun ageRangeLinkBuilder(uriComponentsBuilderFactory: UriComponentsBuilderFactory): AgeRangeLinkBuilder {
        return AgeRangeLinkBuilder(uriComponentsBuilderFactory)
    }

    @Bean
    fun ageRangeResourceConverter(ageRangeLinkBuilder: AgeRangeLinkBuilder): AgeRangeResourceConverter {
        return AgeRangeResourceConverter(ageRangeLinkBuilder)
    }

    @Bean
    fun getAgeRange(ageRangeResourceConverter: AgeRangeResourceConverter): GetAgeRange {
        return GetAgeRange(ageRangeRepository)
    }

    @Bean
    fun contentPartnerUpdatesConverter(): ContentPartnerUpdatesConverter {
        return ContentPartnerUpdatesConverter(legalRestrictionsRepository, ageRangeRepository)
    }

    @Bean
    fun signedLinkProvider(config: GcsProperties): SignedLinkProvider {
        return GcsSignedLinkProvider(config)
    }
}
