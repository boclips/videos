package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.service.application.video.VideoRetrievalService
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video

class VideoCreationService(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository,
    private val videoDuplicationService: VideoDuplicationService
) {
    fun create(videoToBeCreated: Video, user: User): Video {
        if (videoRepository.existsVideoFromChannelId(
                videoToBeCreated.channel.channelId,
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
                    value = videoToBeCreated.channel.channelId.value
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

        val duplicatedVideo = videoRepository.findVideoByTitleFromChannelName(
            videoToBeCreated.channel.name, videoToBeCreated.title
        )

        val newActiveVideo = videoRepository.create(videoToBeCreated.copy(ageRange = ageRange))
        duplicatedVideo?.let { videoDuplicationService.markDuplicate(it.videoId, newActiveVideo.videoId, user) }

        return newActiveVideo
    }
}
