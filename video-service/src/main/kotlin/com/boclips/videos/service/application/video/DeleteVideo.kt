package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoDeletionService
import mu.KLogging

class DeleteVideo(
    private val videoDeletionService: VideoDeletionService
) {
    companion object : KLogging()

    operator fun invoke(id: String?, user: User) {
        if (id == null || id.isBlank()) {
            throw VideoNotFoundException()
        }

        val videoId = VideoId(value = id)
        videoDeletionService.delete(videoId = videoId, user = user)
    }
}
