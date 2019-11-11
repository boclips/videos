package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.ContentType

object VideoTypeConverter {

    fun convert(legacyType: ContentType): VideoType {
        return when(legacyType) {
            ContentType.INSTRUCTIONAL_CLIPS -> VideoType.INSTRUCTIONAL
            ContentType.NEWS -> VideoType.NEWS
            ContentType.STOCK -> VideoType.STOCK
        }
    }
}
