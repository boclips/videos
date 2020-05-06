package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.Video

class VideoCreationService(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository
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
                    ageRange = AgeRange.of(this.ageRangeBuckets.min, this.ageRangeBuckets.max, curatedManually = false)
                }
        }

        if (videoToBeCreated.isBoclipsHosted()) {
            playbackRepository.setDefaultThumbnail(videoToBeCreated.playback.id)
        }

        return videoRepository.create(videoToBeCreated.copy(ageRange = ageRange))
    }
}
