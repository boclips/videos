package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.contentpartner.service.domain.model.channel.CustomIngest
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.contentpartner.ChannelMarketingDetails
import com.boclips.eventbus.domain.contentpartner.ChannelPedagogyDetails
import com.boclips.eventbus.domain.contentpartner.ChannelTopLevelDetails
import com.boclips.eventbus.domain.contentpartner.ChannelId
import com.boclips.eventbus.domain.contract.ContractId
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.service.domain.model.subject.Subject
import mu.KLogging
import com.boclips.eventbus.domain.Subject as EventBusSubject
import com.boclips.eventbus.domain.SubjectId as EventBusSubjectId
import com.boclips.eventbus.domain.contentpartner.ChannelIngestDetails as EventBusIngestDetails
import com.boclips.eventbus.domain.contentpartner.Channel as EventBusChannel
import com.boclips.eventbus.domain.contract.Contract as EventBusContract

class EventConverter {
    companion object : KLogging()

    fun toContentPartnerPayload(
        channel: Channel, allSubjects: List<Subject> = listOf()
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
            .ageRange(
                AgeRange.builder()
                    .min(channel.pedagogyInformation?.ageRangeBuckets?.min)
                    .max(channel.pedagogyInformation?.ageRangeBuckets?.max)
                    .build()
            )
            .awards(channel.awards)
            .description(channel.description)
            .contentTypes(channel.contentTypes?.map { it.name })
            .contentCategories(channel.contentCategories)
            .language(channel.language)
            .hubspotId(channel.hubspotId)
            .notes(channel.notes)
            .legalRestrictions(channel.legalRestriction?.text)
            .ingest(toIngestDetailsPayload(channel))
            .deliveryFrequency(channel.deliveryFrequency)
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
            .contentCategories(channel.contentCategories)
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
            .curriculumAligned(pedagogyInformation?.curriculumAligned)
            .educationalResources(pedagogyInformation?.educationalResources)
            .transcriptProvided(pedagogyInformation?.isTranscriptProvided)
            .build()
    }

    fun toIngestDetailsPayload(channel: Channel): EventBusIngestDetails {
        val ingest = channel.ingest
        val (type, urls) = when (ingest) {
            ManualIngest -> IngestType.MANUAL to null
            CustomIngest -> IngestType.CUSTOM to null
            is MrssFeedIngest -> IngestType.MRSS to ingest.urls
            is YoutubeScrapeIngest -> IngestType.YOUTUBE to ingest.playlistIds
        }

        return EventBusIngestDetails
            .builder()
            .type(type.name)
            .urls(urls)
            .deliveryFrequency(channel.deliveryFrequency)
            .build()
    }

    fun toContractPayload(contract: ContentPartnerContract): EventBusContract =
        EventBusContract.builder()
            .contractId(ContractId.builder().value(contract.id.value).build())
            .name(contract.contentPartnerName)
            .build()

    fun toSubjectPayload(subject: Subject): EventBusSubject =
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
