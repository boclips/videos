package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService

class GetVideoById(
    private val videoService: VideoService
) {
    operator fun invoke(videoId: VideoId, user: User): Video {
        return videoService.getPlayableVideo(
            videoId,
            videoAccessRule = user.accessRules.videoAccess
        )
    }
}
