package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.application.exceptions.InvalidContentPartnerNameException
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter
import org.bson.types.ObjectId

class CreateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository
) {
    operator fun invoke(request: ContentPartnerRequest): ContentPartner {
        val ageRange = request.ageRange?.let { AgeRange.bounded(min = it.min, max = it.max) } ?: AgeRange.unbounded()

        if (request.name.isNullOrEmpty()) {
            throw InvalidContentPartnerNameException()
        }

        val methods = request.hiddenFromSearchForDeliveryMethods?.let(
            this::getDeliveryMethodsFromResource
        ) ?: getDeliveryMethodsFromSearchable(request)

        return contentPartnerRepository.create(
            ContentPartner(
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                name = request.name,
                ageRange = ageRange,
                credit = request.accreditedToYtChannelId?.let { Credit.YoutubeCredit(it) } ?: Credit.PartnerCredit,
                searchable = request.searchable ?: searchableFromDeliveryMethods(methods),
                hiddenFromSearchForDeliveryMethods = methods
            )
        )
    }

    private fun getDeliveryMethodsFromSearchable(
        request: ContentPartnerRequest
    ): Set<DeliveryMethod> {
        return if (request.searchable == null || request.searchable) {
            emptySet()
        } else {
            DeliveryMethod.ALL
        }
    }

    private fun getDeliveryMethodsFromResource(methods: Set<DeliveryMethodResource>): Set<DeliveryMethod> {
        return methods.map(
            DeliveryMethodResourceConverter::fromResource
        ).toSet()
    }

    private fun searchableFromDeliveryMethods(methods: Set<DeliveryMethod>?): Boolean =
        methods != DeliveryMethod.ALL
}
