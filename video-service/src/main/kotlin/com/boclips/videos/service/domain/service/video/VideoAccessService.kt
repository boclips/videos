package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository

class VideoAccessService(
    private val videoRepository: VideoRepository
) {
    fun grantAccess(videoIds: List<VideoId>) {
        setSearchBlacklist(videoIds, emptySet())
    }

    fun revokeAccess(videoIds: List<VideoId>) {
        setSearchBlacklist(videoIds, DeliveryMethod.ALL)
    }

    fun setSearchBlacklist(videoIds: List<VideoId>, deliveryMethods: Set<DeliveryMethod>) {
        update(videoIds) { videoId ->
            listOf(
                VideoUpdateCommand.UpdateHiddenFromSearchForDeliveryMethods(
                    videoId = videoId,
                    deliveryMethods = deliveryMethods
                )
            )
        }
    }

    private fun update(videoIds: List<VideoId>, command: (videoId: VideoId) -> List<VideoUpdateCommand>) {
        videoRepository.bulkUpdate(videoIds.flatMap(command))
    }
}
