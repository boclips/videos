package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.video.VideoService

class GetVideoById(
    private val videoService: VideoService,
    private val accessRuleService: AccessRuleService
) {
    operator fun invoke(videoId: VideoId, user: User): Video {
        val accessRule = accessRuleService.getRules(user)

        return videoService.getPlayableVideo(
            videoId,
            videoAccessRule = accessRule.videoAccess
        )
    }
}
