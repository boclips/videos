package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import org.bson.types.ObjectId

class CreateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository
) {
    operator fun invoke(request: ContentPartnerRequest): ContentPartner {
        val ageRange = request.ageRange?.let { AgeRange.bounded(min = it.min, max = it.max) } ?: AgeRange.unbounded()

        return contentPartnerRepository.create(
            ContentPartner(
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                name = request.name,
                ageRange = ageRange
            )
        )
    }
}