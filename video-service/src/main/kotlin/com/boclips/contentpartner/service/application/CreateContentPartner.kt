package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.contentpartner.service.presentation.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import org.bson.types.ObjectId
import java.util.Currency

class CreateContentPartner(private val contentPartnerRepository: ContentPartnerRepository) {
    operator fun invoke(request: ContentPartnerRequest): ContentPartner {
        val ageRange = request.ageRange?.let {
            AgeRange
                .bounded(min = it.min, max = it.max)
        } ?: AgeRange.unbounded()

        val methods = request.distributionMethods?.let(
            DistributionMethodResourceConverter::toDistributionMethods
        ) ?: DistributionMethod.ALL

        val filters = ContentPartnerFiltersConverter.convert(
            name = request.name!!,
            official = request.accreditedToYtChannelId == null,
            accreditedYTChannelId = request.accreditedToYtChannelId
        )

        if (contentPartnerRepository.findAll(filters).toList().isNotEmpty()) {
            throw ContentPartnerConflictException(
                request.name
            )
        }

        return contentPartnerRepository
            .create(
                ContentPartner(
                    contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                    name = request.name,
                    ageRange = ageRange,
                    credit = request.accreditedToYtChannelId?.let {
                        Credit
                            .YoutubeCredit(it)
                    } ?: Credit.PartnerCredit,
                    legalRestrictions = null,
                    distributionMethods = methods,
                    remittance = request.currency?.let { Remittance(Currency.getInstance(it)) }
                )
            )
    }
}
