package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetVideosByQuery(
        private val videoService: VideoService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    fun execute(query: String?): List<VideoResource> {
        query ?: throw QueryValidationException()

        return videoService.search(VideoSearchQuery(text = query))
                .let(videoToResourceConverter::convert)
    }
}