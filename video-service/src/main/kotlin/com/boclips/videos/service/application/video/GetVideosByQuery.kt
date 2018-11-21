package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideosResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetVideosByQuery(
        private val videoService: VideoService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    fun execute(query: String?, pageIndex: Int, pageSize: Int): VideosResource {
        query ?: throw QueryValidationException()

        val videoSearchQuery = VideoSearchQuery(text = query, pageIndex = pageIndex, pageSize = pageSize)

        val videoCount = videoService.count(videoSearchQuery)
        val videos: List<Video> = videoService.search(videoSearchQuery)

        return videoToResourceConverter.convert(videos, videoCount)
    }
}