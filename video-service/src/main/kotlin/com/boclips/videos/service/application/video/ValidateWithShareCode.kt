package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.InvalidShareCodeException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService

class ValidateWithShareCode(private val videoService: VideoService) {
    operator fun invoke(videoId: String, shareCode: String) {
        val video = videoService.getPlayableVideo(VideoId(videoId))

        if (!shareCodeIsValid(video, shareCode)) {
            throw InvalidShareCodeException(shareCode, videoId)
        }
    }

    private fun shareCodeIsValid(video: Video, shareCode: String): Boolean {
        return video.shareCodes?.let {
            it.contains(shareCode)
        } ?: false
    }
}
