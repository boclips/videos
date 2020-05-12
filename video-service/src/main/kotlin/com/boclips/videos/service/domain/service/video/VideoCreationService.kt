package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.video.Video

class VideoCreationService(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository
) {
    fun create(videoToBeCreated: Video): Video {
        if (videoRepository.existsVideoFromContentPartnerId(
                videoToBeCreated.contentPartner.contentPartnerId,
                videoToBeCreated.videoReference
            )
        ) {
            VideoRetrievalService.logger.info { "Detected duplicate for $videoToBeCreated." }
            throw VideoNotCreatedException(videoToBeCreated)
        }

        var ageRange = videoToBeCreated.ageRange
        if (videoToBeCreated.ageRange is UnknownAgeRange) {
            contentPartnerRepository.findById(
                    contentPartnerId = ContentPartnerId(
                        value = videoToBeCreated.contentPartner.contentPartnerId.value
                    )
                )
                ?.apply {
                    ageRange = AgeRange.of(
                        this.pedagogyInformation?.ageRangeBuckets?.min,
                        this.pedagogyInformation?.ageRangeBuckets?.max,
                        curatedManually = false
                    )
                }
        }

        return videoRepository.create(videoToBeCreated.copy(ageRange = ageRange))
    }
}
