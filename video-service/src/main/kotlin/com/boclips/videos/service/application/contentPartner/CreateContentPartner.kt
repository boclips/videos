package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.contentPartner.CreateContentPartnerRequest
import org.bson.types.ObjectId

class CreateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoService: VideoService
) {
    operator fun invoke(request: CreateContentPartnerRequest): ContentPartner {
        val ageRange = request.ageRange?.let { AgeRange.bounded(min= it.min, max = it.max) } ?: AgeRange.unbounded()

        val contentPartner = contentPartnerRepository.create(
            ContentPartner(
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                name = request.name,
                ageRange = ageRange
            )
        )

        videoService.getVideosByContentPartner(contentPartner.name).forEach {
            videoService.setDefaultAgeRange(it.videoId, contentPartner.ageRange)
        }

        return contentPartner
    }
}