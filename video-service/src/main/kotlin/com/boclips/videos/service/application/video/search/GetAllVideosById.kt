package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.hateoas.Resource

class GetAllVideosById(
    private val videoService: VideoService,
    private val videoToResourceConverter: VideoToResourceConverter
) {

    operator fun invoke(videoIds: List<AssetId>): List<Resource<VideoResource>> {
        return videoService.get(videoIds.toSet().toList())
            .let(videoToResourceConverter::convert)
    }
}