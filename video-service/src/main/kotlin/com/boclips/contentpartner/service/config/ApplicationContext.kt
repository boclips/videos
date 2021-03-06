package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.agerange.GetAgeRange
import com.boclips.contentpartner.service.application.channel.*
import com.boclips.contentpartner.service.application.contract.*
import com.boclips.contentpartner.service.application.contract.legalrestrictions.BroadcastContractLegalRestrictions
import com.boclips.contentpartner.service.application.contract.legalrestrictions.CreateContractLegalRestriction
import com.boclips.contentpartner.service.application.contract.legalrestrictions.FindAllContractLegalRestrictions
import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.contentpartner.service.domain.service.contract.ContractService
import com.boclips.contentpartner.service.infrastructure.signedlink.ContentPartnerContractsSignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.signedlink.ContentPartnerMarketingSignedLinkProvider
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeResourceConverter
import com.boclips.contentpartner.service.presentation.converters.ContractLegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.*
import com.boclips.contentpartner.service.presentation.hateoas.ContractLegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContractsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.eventbus.EventBus
import com.boclips.videos.service.application.GetCategoryWithAncestors
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerApplicationContext")
class ApplicationContext(
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val channelRepository: ChannelRepository,
    val channelService: ChannelService,
    val ageRangeRepository: AgeRangeRepository,
    val contractRepository: ContractRepository,
    val contractLegalRestrictionsRepository: ContractLegalRestrictionsRepository,
    val subjectRepository: SubjectRepository,
    val eventConverter: EventConverter,
    val eventBus: EventBus,
    val channelIndex: ChannelIndex
) {
    @Bean
    fun getChannel(): GetChannel {
        return GetChannel(
            channelRepository
        )
    }

    @Bean
    fun getChannels(): GetChannels {
        return GetChannels(
            channelService
        )
    }

    @Bean
    fun createChannel(getCategoryWithAncestors: GetCategoryWithAncestors): CreateChannel {
        return CreateChannel(
            channelService,
            ageRangeRepository,
            ingestDetailsToResourceConverter(),
            contractRepository,
            getCategoryWithAncestors
        )
    }

    @Bean
    fun contractUpdate(): ContractUpdated =
        ContractUpdated(
            contractRepository = contractRepository,
            channelRepository = channelRepository
        )

    @Bean
    fun updateChannelIndex(): UpdateChannelIndex =
        UpdateChannelIndex(channelRepository = channelRepository, channelIndex = channelIndex)

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
    fun getContract(): GetContract {
        return GetContract(
            contractRepository
        )
    }

    @Bean
    fun createContract(contractService: ContractService): CreateContract {
        return CreateContract(contractService)
    }

    @Bean
    fun contractToResourceConverter(
        contractsLinkBuilder: ContractsLinkBuilder
    ): ContractToResourceConverter {
        return ContractToResourceConverter(
            contractsLinkBuilder
        )
    }

    @Bean
    fun contractConverter(): ContractConverter {
        return ContractConverter(
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
        BroadcastContracts(eventBus, eventConverter, contractRepository)

    @Bean
    fun broadcastContractLegalRestrictions(): BroadcastContractLegalRestrictions =
        BroadcastContractLegalRestrictions(eventBus, contractLegalRestrictionsRepository)

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
    fun channelUpdatesConverter(getCategoryWithAncestors: GetCategoryWithAncestors): ChannelUpdatesConverter {
        return ChannelUpdatesConverter(
            legalRestrictionsRepository,
            ageRangeRepository,
            ingestDetailsToResourceConverter(),
            contractRepository,
            getCategoryWithAncestors
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
        return ContentPartnerContractsSignedLinkProvider(
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
    fun getAllContracts(
        contractRepository: ContractRepository
    ): GetContracts =
        GetContracts(
            contractRepository
        )
}
