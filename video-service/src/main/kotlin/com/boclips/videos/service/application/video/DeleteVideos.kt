package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.VideoService

class DeleteVideos(
        private val videoService: VideoService
) {
    fun execute(id: String?) {
        if (id == null || id.isBlank()) {
            throw VideoNotFoundException()
        }
        val video = videoService.findVideoBy(VideoId(videoId = id))
        videoService.removeVideo(video)
    }
}