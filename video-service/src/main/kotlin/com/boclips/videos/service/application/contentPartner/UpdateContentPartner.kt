package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest

class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoService: VideoService
) {
    operator fun invoke(existingContentPartnerId: String, request: ContentPartnerRequest): ContentPartner {
        val ageRange = request.ageRange?.let { AgeRange.bounded(min = it.min, max = it.max) } ?: AgeRange.unbounded()

        val contentPartner = contentPartnerRepository.update(
            ContentPartner(
                contentPartnerId = ContentPartnerId(existingContentPartnerId),
                name = request.name,
                ageRange = ageRange
            )
        )

        videoService.setDefaultAgeRange(contentPartner)

        return contentPartner
    }
}