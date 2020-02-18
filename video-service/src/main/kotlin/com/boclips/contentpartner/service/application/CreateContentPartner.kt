package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.domain.model.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.MarketingInformation
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.presentation.ContentPartnerStatusConverter
import com.boclips.contentpartner.service.presentation.DistributionMethodResourceConverter
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import com.boclips.videos.service.domain.model.video.ContentCategories
import org.bson.types.ObjectId
import java.util.Currency
import java.util.Locale

class CreateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val ageRangeRepository: AgeRangeRepository
) {
    operator fun invoke(upsertRequest: UpsertContentPartnerRequest): ContentPartner {
        val ageRanges = upsertRequest.ageRanges?.let {
            it.map { rawAgeRangeId ->
                AgeRangeId(rawAgeRangeId).let { ageRangeId ->
                    ageRangeRepository.findById(ageRangeId) ?: throw InvalidAgeRangeException(ageRangeId)
                }
            }
        } ?: emptyList()

        val methods = upsertRequest.distributionMethods?.let(
            DistributionMethodResourceConverter::toDistributionMethods
        ) ?: DistributionMethod.ALL

        val name = upsertRequest.name!!

        val filters = ContentPartnerFiltersConverter.convert(
            name = name,
            official = upsertRequest.accreditedToYtChannelId == null,
            accreditedYTChannelId = upsertRequest.accreditedToYtChannelId
        )

        if (contentPartnerRepository.findAll(filters).toList().isNotEmpty()) {
            throw ContentPartnerConflictException(name)
        }

        if (!upsertRequest.contentCategories.isNullOrEmpty()) {
            if (upsertRequest.contentCategories?.any { request -> request !in ContentCategories.values().map { it.name } }!!) {
                throw InvalidContentCategoryException()
            }
        }

        return contentPartnerRepository
            .create(
                ContentPartner(
                    contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                    name = name,
                    ageRangeBuckets = AgeRangeBuckets(ageRanges),
                    credit = upsertRequest.accreditedToYtChannelId?.let {
                        Credit
                            .YoutubeCredit(it)
                    } ?: Credit.PartnerCredit,
                    legalRestriction = null,
                    distributionMethods = methods,
                    remittance = upsertRequest.currency?.let { Remittance(Currency.getInstance(it)) },
                    description = upsertRequest.description,
                    contentCategories = upsertRequest.contentCategories,
                    hubspotId = upsertRequest.hubspotId,
                    awards = upsertRequest.awards,
                    notes = upsertRequest.notes,
                    language = upsertRequest.language?.let(Locale::forLanguageTag),
                    contentTypes = upsertRequest.contentTypes?.map(ContentPartnerType::valueOf),
                    marketingInformation = MarketingInformation(
                        oneLineDescription = upsertRequest.oneLineDescription,
                        status = upsertRequest.marketingInformation?.status?.let(ContentPartnerStatusConverter::convert)
                    ),
                    isTranscriptProvided = upsertRequest.isTranscriptProvided
                )
            )
    }
}
