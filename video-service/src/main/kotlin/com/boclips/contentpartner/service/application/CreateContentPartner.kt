package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.presentation.DistributionMethodResourceConverter
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.service.domain.model.video.ContentCategories
import org.bson.types.ObjectId
import java.util.Currency
import java.util.Locale

class CreateContentPartner(private val contentPartnerRepository: ContentPartnerRepository) {
    operator fun invoke(createRequest: CreateContentPartnerRequest): ContentPartner {
        val ageRange = createRequest.ageRange?.let {
            AgeRange
                .bounded(min = it.min, max = it.max)
        } ?: AgeRange.unbounded()

        val methods = createRequest.distributionMethods?.let(
            DistributionMethodResourceConverter::toDistributionMethods
        ) ?: DistributionMethod.ALL

        val name = createRequest.name!!

        val filters = ContentPartnerFiltersConverter.convert(
            name = name,
            official = createRequest.accreditedToYtChannelId == null,
            accreditedYTChannelId = createRequest.accreditedToYtChannelId
        )

        if (contentPartnerRepository.findAll(filters).toList().isNotEmpty()) {
            throw ContentPartnerConflictException(name)
        }

        if(!createRequest.contentCategories.isNullOrEmpty()) {
            if (createRequest.contentCategories?.any { request -> request !in ContentCategories.values().map { it.name } }!!) {
                throw InvalidContentCategoryException()
            }
        }

        return contentPartnerRepository
            .create(
                ContentPartner(
                    contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                    name = name,
                    ageRange = ageRange,
                    credit = createRequest.accreditedToYtChannelId?.let {
                        Credit
                            .YoutubeCredit(it)
                    } ?: Credit.PartnerCredit,
                    legalRestriction = null,
                    distributionMethods = methods,
                    remittance = createRequest.currency?.let { Remittance(Currency.getInstance(it)) },
                    description = createRequest.description,
                    contentCategories = createRequest.contentCategories,
                    hubspotId = createRequest.hubspotId,
                    awards = createRequest.awards,
                    notes = createRequest.notes,
                    language = createRequest.language?.let { Locale.forLanguageTag(it) }
                )
            )
    }
}
