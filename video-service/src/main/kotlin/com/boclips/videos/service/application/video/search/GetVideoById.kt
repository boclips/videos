package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.application.video.VideoRetrievalService

class GetVideoById(
    private val videoRetrievalService: VideoRetrievalService
) {
    operator fun invoke(videoId: VideoId, user: User): Video {
        return videoRetrievalService.getPlayableVideo(
            videoId,
            videoAccess = user.accessRules.videoAccess
        )
    }
}
