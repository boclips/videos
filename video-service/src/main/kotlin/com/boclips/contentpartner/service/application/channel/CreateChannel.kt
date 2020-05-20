package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.exceptions.ContentPartnerHubspotIdExceptions
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.application.exceptions.MissingContentPartnerContractException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.Credit
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.Remittance
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerMarketingInformationConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import com.boclips.videos.service.domain.model.video.ContentCategories
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import org.bson.types.ObjectId
import java.util.Currency
import java.util.Locale

class CreateChannel(
    private val channelRepository: ChannelRepository,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsToResourceConverter: IngestDetailsResourceConverter,
    private val contentPartnerContractRepository: ContentPartnerContractRepository,
    private val subjectRepository: SubjectRepository,
    private val eventConverter: EventConverter,
    private val eventBus: EventBus
) {
    operator fun invoke(upsertRequest: ContentPartnerRequest): Channel {
        val ageRanges = upsertRequest.ageRanges.orEmpty().map { rawAgeRangeId ->
            AgeRangeId(rawAgeRangeId).let { ageRangeId ->
                ageRangeRepository.findById(ageRangeId) ?: throw InvalidAgeRangeException(ageRangeId)
            }
        }

        val allSubjects = subjectRepository.findAll()

        val methods = upsertRequest.distributionMethods?.let(
            DistributionMethodResourceConverter::toDistributionMethods
        ) ?: DistributionMethod.ALL

        val name = upsertRequest.name!!
        val hubspotId = upsertRequest.hubspotId

        val validityFilter =
            ChannelFiltersConverter.convert(
                name = name,
                hubspotId = hubspotId,
                official = upsertRequest.accreditedToYtChannelId == null,
                accreditedYTChannelId = upsertRequest.accreditedToYtChannelId
            )

        val validateChannel = channelRepository.findAll(validityFilter).toList()

        if (validateChannel.isNotEmpty()) {
            validateChannel.forEach {
                if (it.name == name) {
                    throw ContentPartnerConflictException(name)
                }
                if (it.hubspotId == hubspotId && hubspotId != null) {
                    throw ContentPartnerHubspotIdExceptions(hubspotId)
                }
            }
        }

        if (!upsertRequest.contentCategories.isNullOrEmpty()) {
            if (upsertRequest.contentCategories?.any { request ->
                    request !in ContentCategories.values().map { it.name }
                }!!) {
                throw InvalidContentCategoryException()
            }
        }

        val contract = upsertRequest.contractId?.let {
            val contractId = ContentPartnerContractId(it)

            contentPartnerContractRepository.findById(contractId)
                ?: throw InvalidContractException(contractId)
        }

        if(upsertRequest.ingest?.type != IngestType.YOUTUBE) {
            if(upsertRequest.contractId.isNullOrBlank()) {
                throw MissingContentPartnerContractException()
            }
        }

        val createdChannel = channelRepository
            .create(
                Channel(
                    id = ChannelId(
                        value = ObjectId().toHexString()
                    ),
                    name = name,
                    credit = upsertRequest.accreditedToYtChannelId?.let {
                        Credit
                            .YoutubeCredit(it)
                    } ?: Credit.PartnerCredit,
                    legalRestriction = null,
                    distributionMethods = methods,
                    remittance = upsertRequest.currency?.let {
                        Remittance(
                            Currency.getInstance(it)
                        )
                    },
                    description = upsertRequest.description,
                    contentCategories = upsertRequest.contentCategories,
                    hubspotId = upsertRequest.hubspotId,
                    awards = upsertRequest.awards,
                    notes = upsertRequest.notes,
                    language = upsertRequest.language?.let(Locale::forLanguageTag),
                    contentTypes = upsertRequest.contentTypes?.mapNotNull {
                        when (it) {
                            "NEWS" -> ContentType.NEWS
                            "INSTRUCTIONAL" -> ContentType.INSTRUCTIONAL
                            "STOCK" -> ContentType.STOCK
                            else -> null
                        }
                    },
                    ingest = upsertRequest.ingest?.let { ingestDetailsToResourceConverter.fromResource(it) }
                        ?: ManualIngest,
                    deliveryFrequency = upsertRequest.deliveryFrequency,
                    pedagogyInformation = PedagogyInformation(
                        isTranscriptProvided = upsertRequest.isTranscriptProvided,
                        educationalResources = upsertRequest.educationalResources,
                        curriculumAligned = upsertRequest.curriculumAligned,
                        bestForTags = upsertRequest.bestForTags,
                        subjects = upsertRequest.subjects,
                        ageRangeBuckets = AgeRangeBuckets(
                            ageRanges
                        )
                    ),
                    marketingInformation = ContentPartnerMarketingInformationConverter.convert(upsertRequest),
                    contract = contract
                )
            )

        eventBus.publish(
            ContentPartnerUpdated
                .builder()
                .contentPartner(eventConverter.toContentPartnerPayload(createdChannel, allSubjects))
                .build()
        )

        return createdChannel
    }
}
