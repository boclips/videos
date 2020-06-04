package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.agerange.GetAgeRange
import com.boclips.contentpartner.service.application.channel.BroadcastChannels
import com.boclips.contentpartner.service.application.channel.ChannelUpdatesConverter
import com.boclips.contentpartner.service.application.channel.ContractUpdated
import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.contentpartnercontract.BroadcastContracts
import com.boclips.contentpartner.service.application.contentpartnercontract.ContractContentPartnerConverter
import com.boclips.contentpartner.service.application.contentpartnercontract.CreateContentPartnerContract
import com.boclips.contentpartner.service.application.contentpartnercontract.GetContentPartnerContract
import com.boclips.contentpartner.service.application.contentpartnercontract.GetContentPartnerContracts
import com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions.CreateContractLegalRestriction
import com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions.FindAllContractLegalRestrictions
import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.domain.service.contentpartnercontract.ContractService
import com.boclips.contentpartner.service.infrastructure.signedlink.ContentPartnerContractSignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.signedlink.ContentPartnerMarketingSignedLinkProvider
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeResourceConverter
import com.boclips.contentpartner.service.presentation.converters.ContractLegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContentPartnerContractToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractCostsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractDatesToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRemittanceCurrencyConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRestrictionsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRoyaltySplitConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnerContractsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContractLegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.eventbus.EventBus
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerApplicationContext")
class ApplicationContext(
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val channelRepository: ChannelRepository,
    val ageRangeRepository: AgeRangeRepository,
    val contentPartnerContractRepository: ContentPartnerContractRepository,
    val contractLegalRestrictionsRepository: ContractLegalRestrictionsRepository,
    val subjectRepository: SubjectRepository,
    val eventConverter: EventConverter,
    val eventBus: EventBus
) {
    @Bean
    fun getContentPartner(): GetChannel {
        return GetChannel(
            channelRepository
        )
    }

    @Bean
    fun getContentPartners(): GetChannels {
        return GetChannels(
            channelRepository
        )
    }

    @Bean
    fun createContentPartner(): CreateChannel {
        return CreateChannel(
            channelRepository,
            ageRangeRepository,
            ingestDetailsToResourceConverter(),
            contentPartnerContractRepository,
            subjectRepository,
            eventConverter,
            eventBus
        )
    }

    @Bean
    fun contractUpdate(): ContractUpdated =
        ContractUpdated(
            contractRepository = contentPartnerContractRepository,
            channelRepository = channelRepository
        )

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
    fun createContentPartnerContract(contractService: ContractService): CreateContentPartnerContract {
        return CreateContentPartnerContract(contractService)
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
    fun contractContentPartnerConverter(): ContractContentPartnerConverter {
        return ContractContentPartnerConverter(
            contractDatesConverter(),
            royaltySplitConverter(),
            remittanceCurrencyConverter(),
            restrictionsConverter(),
            costsConverter()
        )
    }

    @Bean
    fun broadcastChannels(
        eventBus: EventBus,
        eventConverter: EventConverter,
        channelRepository: ChannelRepository,
        subjectRepository: SubjectRepository
    ): BroadcastChannels {
        return BroadcastChannels(
            eventBus,
            eventConverter,
            channelRepository,
            subjectRepository
        )
    }

    @Bean
    fun broadcastContracts(): BroadcastContracts =
        BroadcastContracts(eventBus, eventConverter, contentPartnerContractRepository)

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
    fun contentPartnerUpdatesConverter(): ChannelUpdatesConverter {
        return ChannelUpdatesConverter(
            legalRestrictionsRepository,
            ageRangeRepository,
            ingestDetailsToResourceConverter(),
            contentPartnerContractRepository
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
    fun contractDatesConverter(): ContractDatesToResourceConverter = ContractDatesToResourceConverter()

    @Bean
    fun royaltySplitConverter(): ContractRoyaltySplitConverter = ContractRoyaltySplitConverter()

    @Bean
    fun remittanceCurrencyConverter(): ContractRemittanceCurrencyConverter = ContractRemittanceCurrencyConverter()

    @Bean
    fun restrictionsConverter(): ContractRestrictionsConverter = ContractRestrictionsConverter()

    @Bean
    fun costsConverter(): ContractCostsConverter = ContractCostsConverter()

    @Bean
    fun getAllContentPartnerContracts(
        contentPartnerContractRepository: ContentPartnerContractRepository
    ): GetContentPartnerContracts =
        GetContentPartnerContracts(
            contentPartnerContractRepository
        )
}
