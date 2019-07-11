package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository

class VideoAccessService(
    private val videoRepository: VideoRepository
) {
    fun grantAccess(videoIds: List<VideoId>) {
        setSearchBlacklist(videoIds, emptySet())
    }

    fun revokeAccess(videoIds: List<VideoId>) {
        setSearchBlacklist(videoIds, DistributionMethod.ALL)
    }

    fun setSearchBlacklist(videoIds: List<VideoId>, distributionMethods: Set<DistributionMethod>) {
        update(videoIds) { videoId ->
            listOf(
                VideoUpdateCommand.UpdateHiddenFromSearchForDeliveryMethods(
                    videoId = videoId,
                    distributionMethods = distributionMethods
                )
            )
        }
    }

    private fun update(videoIds: List<VideoId>, command: (videoId: VideoId) -> List<VideoUpdateCommand>) {
        videoRepository.bulkUpdate(videoIds.flatMap(command))
    }
}
