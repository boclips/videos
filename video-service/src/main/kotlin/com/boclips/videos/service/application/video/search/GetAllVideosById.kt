package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.hateoas.Resource

class GetAllVideosById(
    private val videoService: VideoService,
    private val videoToResourceConverter: VideoToResourceConverter
) {

    operator fun invoke(videoIds: List<VideoId>): List<Resource<VideoResource>> {
        return videoService.getPlayableVideo(videoIds.toSet().toList())
            .let(videoToResourceConverter::wrapVideosInResource)
    }
}