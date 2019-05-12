package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.hateoas.Resource

class GetVideoById(
    private val videoService: VideoService,
    private val videoToResourceConverter: VideoToResourceConverter
) {

    operator fun invoke(videoId: VideoId): Resource<VideoResource> {
        return videoService.getPlayableVideo(videoId)
            .let(videoToResourceConverter::fromVideo)
    }
}