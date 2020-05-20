package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.video.Video

class VideoCreationService(
    private val channelRepository: ChannelRepository,
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
            channelRepository.findById(
                channelId = ChannelId(
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
