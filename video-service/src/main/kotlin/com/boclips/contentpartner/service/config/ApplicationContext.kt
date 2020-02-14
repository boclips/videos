package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.application.ContentPartnerUpdatesConverter
import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.CreateEduAgeRange
import com.boclips.contentpartner.service.application.EduAgeRangeResourceConverter
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.application.GetEduAgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.EduAgeRangeRepository
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.EduAgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerApplicationContext")
class ApplicationContext(
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val contentPartnerRepository: ContentPartnerRepository,
    val eduAgeRangeRepository: EduAgeRangeRepository
) {
    @Bean
    fun getContentPartnerToResourceConverter(
        contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
        legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter
    ): ContentPartnerToResourceConverter {
        return ContentPartnerToResourceConverter(contentPartnersLinkBuilder, legalRestrictionsToResourceConverter)
    }

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
        return CreateContentPartner(contentPartnerRepository, eduAgeRangeRepository)
    }

    @Bean
    fun createEduAgeRange(): CreateEduAgeRange {
        return CreateEduAgeRange(eduAgeRangeRepository)
    }

    @Bean
    fun eduAgeRangeLinkBuilder(uriComponentsBuilderFactory: UriComponentsBuilderFactory): EduAgeRangeLinkBuilder {
        return EduAgeRangeLinkBuilder(uriComponentsBuilderFactory)
    }

    @Bean
    fun eduAgeRangeResourceConverter(eduAgeRangeLinkBuilder: EduAgeRangeLinkBuilder): EduAgeRangeResourceConverter {
        return EduAgeRangeResourceConverter(eduAgeRangeLinkBuilder)
    }

    @Bean
    fun getEduAgeRange(eduAgeRangeResourceConverter: EduAgeRangeResourceConverter): GetEduAgeRange {
        return GetEduAgeRange(eduAgeRangeRepository)
    }

    @Bean
    fun contentPartnerUpdatesConverter(): ContentPartnerUpdatesConverter {
        return ContentPartnerUpdatesConverter(legalRestrictionsRepository, eduAgeRangeRepository)
    }
}
