package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.exceptions.ContentPartnerHubspotIdExceptions
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.contentpartner.Credit
import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.Remittance
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerMarketingInformationConverter
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import com.boclips.videos.service.domain.model.video.ContentCategories
import org.bson.types.ObjectId
import java.util.Currency
import java.util.Locale

class CreateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsToResourceConverter: IngestDetailsResourceConverter,
    private val contentPartnerContractRepository: ContentPartnerContractRepository
) {
    operator fun invoke(upsertRequest: ContentPartnerRequest): ContentPartner {
        val ageRanges = upsertRequest.ageRanges.orEmpty().map { rawAgeRangeId ->
            AgeRangeId(rawAgeRangeId).let { ageRangeId ->
                ageRangeRepository.findById(ageRangeId) ?: throw InvalidAgeRangeException(ageRangeId)
            }
        }

        val methods = upsertRequest.distributionMethods?.let(
            DistributionMethodResourceConverter::toDistributionMethods
        ) ?: DistributionMethod.ALL

        val name = upsertRequest.name!!
        val hubspotId = upsertRequest.hubspotId

        val validityFilter = ContentPartnerFiltersConverter.convert(
            name = name,
            hubspotId = hubspotId,
            official = upsertRequest.accreditedToYtChannelId == null,
            accreditedYTChannelId = upsertRequest.accreditedToYtChannelId
        )

        val validateContentPartner = contentPartnerRepository.findAll(validityFilter).toList()

        if (validateContentPartner.isNotEmpty()) {
            validateContentPartner.forEach {
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

        return contentPartnerRepository
            .create(
                ContentPartner(
                    contentPartnerId = ContentPartnerId(
                        value = ObjectId().toHexString()
                    ),
                    name = name,
                    ageRangeBuckets = AgeRangeBuckets(
                        ageRanges
                    ),
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
                            "NEWS" -> ContentPartnerType.NEWS
                            "INSTRUCTIONAL" -> ContentPartnerType.INSTRUCTIONAL
                            "STOCK" -> ContentPartnerType.STOCK
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
    }
}
