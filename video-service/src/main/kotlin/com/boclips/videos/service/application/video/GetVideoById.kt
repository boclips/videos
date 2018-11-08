package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.PlaybackService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetVideoById(
        private val videoService: VideoService,
        private val playbackService: PlaybackService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    fun execute(videoId: String): VideoResource {
        val videoWithoutPlayback = videoService.findVideoBy(VideoId(videoId = videoId))
        val videoWithPlayback: Video = playbackService.getVideoWithPlayback(videoWithoutPlayback)

        return videoToResourceConverter.convert(videoWithPlayback)
    }
}