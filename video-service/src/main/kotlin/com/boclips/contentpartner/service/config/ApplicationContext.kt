package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.agerange.GetAgeRange
import com.boclips.contentpartner.service.application.contentpartner.ContentPartnerUpdatesConverter
import com.boclips.contentpartner.service.application.contentpartner.CreateContentPartner
import com.boclips.contentpartner.service.application.contentpartner.GetContentPartner
import com.boclips.contentpartner.service.application.contentpartner.GetContentPartners
import com.boclips.contentpartner.service.application.contentpartnercontract.CreateContentPartnerContract
import com.boclips.contentpartner.service.application.contentpartnercontract.GetContentPartnerContract
import com.boclips.contentpartner.service.application.contentpartnercontract.GetContentPartnerContracts
import com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions.CreateContractLegalRestriction
import com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions.FindAllContractLegalRestrictions
import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.signedlink.ContentPartnerContractSignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.signedlink.ContentPartnerMarketingSignedLinkProvider
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeResourceConverter
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerContractToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.ContractLegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnerContractsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContractLegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerApplicationContext")
class ApplicationContext(
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val contentPartnerRepository: ContentPartnerRepository,
    val ageRangeRepository: AgeRangeRepository,
    val contentPartnerContractRepository: ContentPartnerContractRepository,
    val contractLegalRestrictionsRepository: ContractLegalRestrictionsRepository
) {
    @Bean
    fun getContentPartner(): GetContentPartner {
        return GetContentPartner(
            contentPartnerRepository
        )
    }

    @Bean
    fun getContentPartners(): GetContentPartners {
        return GetContentPartners(
            contentPartnerRepository
        )
    }

    @Bean
    fun createContentPartner(): CreateContentPartner {
        return CreateContentPartner(
            contentPartnerRepository,
            ageRangeRepository,
            ingestDetailsToResourceConverter()
        )
    }

    @Bean
    fun findAllContractLegalRestrictions(): FindAllContractLegalRestrictions {
        return FindAllContractLegalRestrictions(
            contractLegalRestrictionsRepository
        )
    }

    @Bean
    fun createContractLegalRestriction(): CreateContractLegalRestriction {
        return CreateContractLegalRestriction(
            contractLegalRestrictionsRepository
        )
    }

    @Bean
    fun contractLegalRestrictionToResourceConverter(): ContractLegalRestrictionsToResourceConverter {
        return ContractLegalRestrictionsToResourceConverter()
    }

    @Bean
    fun contractLegalRestrictionsLinkBuilder(): ContractLegalRestrictionsLinkBuilder {
        return ContractLegalRestrictionsLinkBuilder()
    }

    @Bean
    fun getContentPartnerContract(): GetContentPartnerContract {
        return GetContentPartnerContract(
            contentPartnerContractRepository
        )
    }

    @Bean
    fun createContentPartnerContract(): CreateContentPartnerContract {
        return CreateContentPartnerContract(
            contentPartnerContractRepository
        )
    }

    @Bean
    fun contentPartnerContractToResourceConverter(
        contentPartnerContractsLinkBuilder: ContentPartnerContractsLinkBuilder
    ): ContentPartnerContractToResourceConverter {
        return ContentPartnerContractToResourceConverter(
            contentPartnerContractsLinkBuilder
        )
    }

    @Bean
    fun contentPartnerContractsLinkBuilder(
        uriComponentsBuilderFactory: UriComponentsBuilderFactory
    ): ContentPartnerContractsLinkBuilder {
        return ContentPartnerContractsLinkBuilder(uriComponentsBuilderFactory)
    }

    @Bean
    fun createAgeRange(): CreateAgeRange {
        return CreateAgeRange(
            ageRangeRepository
        )
    }

    @Bean
    fun ageRangeLinkBuilder(uriComponentsBuilderFactory: UriComponentsBuilderFactory): AgeRangeLinkBuilder {
        return AgeRangeLinkBuilder(
            uriComponentsBuilderFactory
        )
    }

    @Bean
    fun ageRangeResourceConverter(ageRangeLinkBuilder: AgeRangeLinkBuilder): AgeRangeResourceConverter {
        return AgeRangeResourceConverter(
            ageRangeLinkBuilder
        )
    }

    @Bean
    fun getAgeRange(ageRangeResourceConverter: AgeRangeResourceConverter): GetAgeRange {
        return GetAgeRange(ageRangeRepository)
    }

    @Bean
    fun contentPartnerUpdatesConverter(): ContentPartnerUpdatesConverter {
        return ContentPartnerUpdatesConverter(
            legalRestrictionsRepository,
            ageRangeRepository,
            ingestDetailsToResourceConverter()
        )
    }

    @Bean
    fun marketingSignedLinkProvider(config: GcsProperties): SignedLinkProvider {
        return ContentPartnerMarketingSignedLinkProvider(
            config
        )
    }

    @Bean
    fun contractSignedLinkProvider(config: GcsProperties): SignedLinkProvider {
        return ContentPartnerContractSignedLinkProvider(
            config
        )
    }

    @Bean
    fun ingestDetailsToResourceConverter(): IngestDetailsResourceConverter =
        IngestDetailsResourceConverter()

    @Bean
    fun getAllContentPartnerContracts(
        contentPartnerContractRepository: ContentPartnerContractRepository
    ): GetContentPartnerContracts =
        GetContentPartnerContracts(
            contentPartnerContractRepository
        )
}
