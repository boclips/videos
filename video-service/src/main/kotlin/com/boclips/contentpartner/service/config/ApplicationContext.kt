package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.application.ContentPartnerUpdatesConverter
import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.ContentPartnersLinkBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerApplicationContext")
class ApplicationContext(
    val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val contentPartnerRepository: ContentPartnerRepository
) {

    @Bean
    fun getContentPartner(): GetContentPartner {
        return GetContentPartner(contentPartnerRepository)
    }

    @Bean
    fun getContentPartners(): GetContentPartners {
        return GetContentPartners(
            contentPartnerRepository,
            contentPartnersLinkBuilder
        )
    }

    @Bean
    fun createContentPartner(): CreateContentPartner {
        return CreateContentPartner(contentPartnerRepository)
    }

    @Bean
    fun contentPartnerUpdatesConverter(): ContentPartnerUpdatesConverter {
        return ContentPartnerUpdatesConverter(legalRestrictionsRepository)
    }

}
