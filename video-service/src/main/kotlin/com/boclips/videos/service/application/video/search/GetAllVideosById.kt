package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetAllVideosById(
        private val videoService: VideoService,
        private val videoToResourceConverter: VideoToResourceConverter
) {

    operator fun invoke(videoIds: Collection<String>): List<VideoResource> {

        return videoService.get(videoIds.map { AssetId(it) })
                .let(videoToResourceConverter::convert)
    }
}