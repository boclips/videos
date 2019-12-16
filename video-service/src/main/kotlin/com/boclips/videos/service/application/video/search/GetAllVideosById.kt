package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService

class GetAllVideosById(
    private val videoService: VideoService
) {
    operator fun invoke(videoIds: List<VideoId>, user: User): List<Video> {
        return videoService.getPlayableVideos(videoIds.toSet().toList(), user.accessRules.videoAccess)
    }
}
