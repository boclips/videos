package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter
import org.bson.types.ObjectId

class CreateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository
) {
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
            throw ContentPartnerConflictException(request.name)
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
                    distributionMethods = methods
                )
            )
    }
}
