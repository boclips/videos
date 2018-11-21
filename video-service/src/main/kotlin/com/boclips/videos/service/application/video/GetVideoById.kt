package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetVideoById(
        private val videoService: VideoService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    fun execute(videoId: String?): VideoResource {
        videoId ?: throw QueryValidationException()

        return videoService.get(AssetId(value = videoId))
                .let(videoToResourceConverter::convert)
    }
}