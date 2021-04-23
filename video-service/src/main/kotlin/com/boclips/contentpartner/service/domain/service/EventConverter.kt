package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.CustomIngest
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contract.ContractDates
import com.boclips.contentpartner.service.domain.model.contract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contract.ContractRoyaltySplit
import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.category.CategoryWithAncestors
import com.boclips.eventbus.domain.contentpartner.ChannelId
import com.boclips.eventbus.domain.contentpartner.ChannelMarketingDetails
import com.boclips.eventbus.domain.contentpartner.ChannelPedagogyDetails
import com.boclips.eventbus.domain.contentpartner.ChannelTopLevelDetails
import com.boclips.eventbus.domain.contract.ContractId
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.service.domain.model.subject.Subject
import mu.KLogging
import com.boclips.eventbus.domain.Subject as EventBusSubject
import com.boclips.eventbus.domain.SubjectId as EventBusSubjectId
import com.boclips.eventbus.domain.contentpartner.Channel as EventBusChannel
import com.boclips.eventbus.domain.contentpartner.ChannelIngestDetails as EventBusIngestDetails
import com.boclips.eventbus.domain.contentpartner.DistributionMethod as EventBusDistributionMethod
import com.boclips.eventbus.domain.contract.Contract as EventBusContract
import com.boclips.eventbus.domain.contract.ContractCosts as EventBusContractCosts
import com.boclips.eventbus.domain.contract.ContractDates as EventBusContractDates
import com.boclips.eventbus.domain.contract.ContractRestrictions as EventBusContractRestrictions
import com.boclips.eventbus.domain.contract.ContractRoyaltySplit as EventBusContractRoyaltySplit

class EventConverter {
    companion object : KLogging()

    fun toContentPartnerPayload(
        channel: Channel,
        allSubjects: List<Subject> = listOf()
    ): EventBusChannel {
        val subjects = channel.pedagogyInformation?.subjects?.mapNotNull {
            allSubjects.find { subject -> subject.id.value == it }
        }

        return EventBusChannel.builder()
            .id(ChannelId(channel.id.value))
            .name(channel.name)
            .details(channelTopLevelDetails(channel))
            .pedagogy(convertPedagogyDetails(channel.pedagogyInformation, subjects))
            .marketing(convertMarketingDetails(channel.marketingInformation))
            .ingest(toIngestDetailsPayload(channel))
            .categories(
                channel.categories.map { category ->
                    CategoryWithAncestors.builder()
                        .code(category.codeValue.value)
                        .description(category.description)
                        .ancestors(category.ancestors.map { it.value }.toSet())
                        .build()
                }.toSet()
            )
            .build()
    }

    private fun convertMarketingDetails(marketingInformation: MarketingInformation?): ChannelMarketingDetails {
        return ChannelMarketingDetails.builder()
            .status(marketingInformation?.status?.name)
            .oneLineIntro(marketingInformation?.oneLineDescription)
            .showreel(marketingInformation?.showreel?.toString())
            .sampleVideos(marketingInformation?.sampleVideos?.map { it.toString() })
            .logos(marketingInformation?.logos?.map { it.toString() })
            .build()
    }

    private fun channelTopLevelDetails(channel: Channel): ChannelTopLevelDetails? {
        return ChannelTopLevelDetails.builder()
            .contentTypes(channel.contentTypes?.map { it.name })
            .contentCategories(channel.contentCategories?.map { it.name })
            .hubspotId(channel.hubspotId)
            .contractId(channel.contract?.id?.value)
            .awards(channel.awards)
            .notes(channel.notes)
            .language(channel.language)
            .build()
    }

    private fun convertPedagogyDetails(
        pedagogyInformation: PedagogyInformation?,
        subjects: List<Subject>?
    ): ChannelPedagogyDetails {
        return ChannelPedagogyDetails.builder()
            .subjects(subjects?.map(::toSubjectPayload))
            .ageRange(
                AgeRange.builder()
                    .min(pedagogyInformation?.ageRangeBuckets?.min)
                    .max(pedagogyInformation?.ageRangeBuckets?.max)
                    .build()
            )
            .bestForTags(pedagogyInformation?.bestForTags)
            .build()
    }

    private fun toIngestDetailsPayload(channel: Channel): EventBusIngestDetails {
        val ingest = channel.ingest
        val (type, urls) = when (ingest) {
            ManualIngest -> IngestType.MANUAL to null
            CustomIngest -> IngestType.CUSTOM to null
            is MrssFeedIngest -> IngestType.MRSS to ingest.urls
            is YoutubeScrapeIngest -> IngestType.YOUTUBE to ingest.playlistIds
        }

        val distributionMethods = channel.distributionMethods.map {
            when (it) {
                DistributionMethod.DOWNLOAD -> EventBusDistributionMethod.DOWNLOAD
                DistributionMethod.STREAM -> EventBusDistributionMethod.STREAM
            }
        }.toSet()

        return EventBusIngestDetails
            .builder()
            .type(type.name)
            .urls(urls)
            .deliveryFrequency(channel.deliveryFrequency)
            .distributionMethods(distributionMethods)
            .build()
    }

    fun toContractPayload(contract: Contract): EventBusContract =
        EventBusContract.builder()
            .contractId(ContractId.builder().value(contract.id.value).build())
            .name(contract.contentPartnerName)
            .apply {
                if (contract.contractDates != null) {
                    contractDates(toContractDatesPayload(contract.contractDates))
                }
            }
            .contractDocument(contract.contractDocument?.toString())
            .contractIsRolling(contract.contractIsRolling)
            .daysBeforeTerminationWarning(contract.daysBeforeTerminationWarning)
            .yearsForMaximumLicense(contract.yearsForMaximumLicense)
            .daysForSellOffPeriod(contract.daysForSellOffPeriod)
            .apply {
                if (contract.royaltySplit != null) {
                    royaltySplit(toContractRoyaltySplitPayload(contract.royaltySplit))
                }
            }
            .minimumPriceDescription(contract.minimumPriceDescription)
            .remittanceCurrency(contract.remittanceCurrency)
            .apply {
                if (contract.restrictions != null) {
                    restrictions(toContractRestrictionsPayload(contract.restrictions))
                }
            }
            .costs(toContractCostsPayload(contract.costs))
            .build()

    private fun toContractDatesPayload(
        contractDates: ContractDates
    ): EventBusContractDates =
        EventBusContractDates.builder()
            .start(contractDates.start)
            .end(contractDates.end)
            .build()

    private fun toContractRoyaltySplitPayload(
        royaltySplit: ContractRoyaltySplit
    ): EventBusContractRoyaltySplit =
        EventBusContractRoyaltySplit.builder()
            .streaming(royaltySplit.streaming)
            .download(royaltySplit.download)
            .build()

    private fun toContractRestrictionsPayload(
        restrictions: ContractRestrictions
    ): EventBusContractRestrictions =
        EventBusContractRestrictions.builder()
            .clientFacing(restrictions.clientFacing)
            .territory(restrictions.territory)
            .editing(restrictions.editing)
            .licensing(restrictions.licensing)
            .marketing(restrictions.marketing)
            .companies(restrictions.companies)
            .payout(restrictions.payout)
            .other(restrictions.other)
            .build()

    private fun toContractCostsPayload(
        costs: ContractCosts
    ): EventBusContractCosts =
        EventBusContractCosts.builder()
            .minimumGuarantee(costs.minimumGuarantee)
            .upfrontLicense(costs.upfrontLicense)
            .technicalFee(costs.technicalFee)
            .recoupable(costs.recoupable)
            .build()

    private fun toSubjectPayload(subject: Subject): EventBusSubject =
        subject.run {
            EventBusSubject.builder()
                .id(
                    EventBusSubjectId
                        .builder()
                        .value(id.value)
                        .build()
                )
                .name(name)
                .build()
        }
}
