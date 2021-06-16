package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.search.service.domain.videos.model.VideoType as SearchVideoType

object VideoTypeConverter {

    fun convert(videoType: VideoType): SearchVideoType {
        return when (videoType) {
            VideoType.INSTRUCTIONAL_CLIPS -> SearchVideoType.INSTRUCTIONAL
            VideoType.NEWS -> SearchVideoType.NEWS
            VideoType.STOCK -> SearchVideoType.STOCK
        }
    }

    fun convert(videoType: SearchVideoType): VideoType {
        return when (videoType) {
            SearchVideoType.INSTRUCTIONAL -> VideoType.INSTRUCTIONAL_CLIPS
            SearchVideoType.NEWS -> VideoType.NEWS
            SearchVideoType.STOCK -> VideoType.STOCK
        }
    }
}
