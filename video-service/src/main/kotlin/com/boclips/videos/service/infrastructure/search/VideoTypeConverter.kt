package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.LegacyVideoType

object VideoTypeConverter {

    fun convert(legacyType: LegacyVideoType): VideoType {
        return when(legacyType) {
            LegacyVideoType.INSTRUCTIONAL_CLIPS -> VideoType.INSTRUCTIONAL
            LegacyVideoType.NEWS -> VideoType.NEWS
            LegacyVideoType.STOCK -> VideoType.STOCK
        }
    }
}
