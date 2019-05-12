package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository

class VideoAccessService(
    private val videoRepository: VideoRepository
) {
    fun accessible(videoId: VideoId): Boolean {
        val video = videoRepository.find(videoId)

        video?.let { return it.searchable } ?: throw VideoNotFoundException()
    }

    fun grantAccess(videoIds: List<VideoId>) {
        videoRepository.bulkUpdate(videoIds.map(VideoUpdateCommand::MakeSearchable))
    }

    fun revokeAccess(videoIds: List<VideoId>) {
        videoRepository.bulkUpdate(videoIds.map(VideoUpdateCommand::HideFromSearch))
    }
}
