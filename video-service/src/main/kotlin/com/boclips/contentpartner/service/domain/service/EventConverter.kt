package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerMarketingInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.CustomIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.YoutubeScrapeIngest
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.contentpartner.ChannelMarketingDetails
import com.boclips.eventbus.domain.contentpartner.ChannelPedagogyDetails
import com.boclips.eventbus.domain.contentpartner.ChannelTopLevelDetails
import com.boclips.eventbus.domain.contentpartner.ContentPartnerId
import com.boclips.eventbus.domain.contract.ContractId
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.service.domain.model.subject.Subject
import mu.KLogging
import com.boclips.eventbus.domain.Subject as EventBusSubject
import com.boclips.eventbus.domain.SubjectId as EventBusSubjectId
import com.boclips.eventbus.domain.contentpartner.ChannelIngestDetails as EventBusIngestDetails
import com.boclips.eventbus.domain.contentpartner.ContentPartner as EventBusContentPartner
import com.boclips.eventbus.domain.contract.Contract as EventBusContract

class EventConverter {
    companion object : KLogging()

    fun toContentPartnerPayload(
        contentPartner: ContentPartner, allSubjects: List<Subject> = listOf()
    ): EventBusContentPartner {
        val subjects = contentPartner.pedagogyInformation?.subjects?.mapNotNull {
            allSubjects.find { subject -> subject.id.value == it }
        }
        return EventBusContentPartner.builder()
            .id(ContentPartnerId(contentPartner.contentPartnerId.value))
            .name(contentPartner.name)
            .details(channelTopLevelDetails(contentPartner))
            .pedagogy(convertPedagogyDetails(contentPartner.pedagogyInformation, subjects))
            .marketing(convertMarketingDetails(contentPartner.marketingInformation))
            .ageRange(
                AgeRange.builder()
                    .min(contentPartner.pedagogyInformation?.ageRangeBuckets?.min)
                    .max(contentPartner.pedagogyInformation?.ageRangeBuckets?.max)
                    .build()
            )
            .awards(contentPartner.awards)
            .description(contentPartner.description)
            .contentTypes(contentPartner.contentTypes?.map { it.name })
            .contentCategories(contentPartner.contentCategories)
            .language(contentPartner.language)
            .hubspotId(contentPartner.hubspotId)
            .notes(contentPartner.notes)
            .legalRestrictions(contentPartner.legalRestriction?.text)
            .ingest(toIngestDetailsPayload(contentPartner))
            .deliveryFrequency(contentPartner.deliveryFrequency)
            .build()
    }

    private fun convertMarketingDetails(marketingInformation: ContentPartnerMarketingInformation?): ChannelMarketingDetails {
        return ChannelMarketingDetails.builder()
            .status(marketingInformation?.status?.name)
            .oneLineIntro(marketingInformation?.oneLineDescription)
            .showreel(marketingInformation?.showreel?.toString())
            .sampleVideos(marketingInformation?.sampleVideos?.map { it.toString() })
            .logos(marketingInformation?.logos?.map { it.toString() })
            .build()
    }

    private fun channelTopLevelDetails(contentPartner: ContentPartner): ChannelTopLevelDetails? {
        return ChannelTopLevelDetails.builder()
            .contentTypes(contentPartner.contentTypes?.map { it.name })
            .contentCategories(contentPartner.contentCategories)
            .hubspotId(contentPartner.hubspotId)
            .contractId(contentPartner.contract?.id?.value)
            .awards(contentPartner.awards)
            .notes(contentPartner.notes)
            .language(contentPartner.language)
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

    fun toIngestDetailsPayload(contentPartner: ContentPartner): EventBusIngestDetails {
        val ingest = contentPartner.ingest
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
            .deliveryFrequency(contentPartner.deliveryFrequency)
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