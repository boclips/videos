package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.LegacyVideoType

object VideoTypeConverter {

    fun convert(legacyType: LegacyVideoType): VideoType {
        return when(legacyType) {
            LegacyVideoType.INSTRUCTIONAL_CLIPS -> VideoType.INSTRUCTIONAL
            LegacyVideoType.TED_TALKS -> VideoType.INSTRUCTIONAL
            LegacyVideoType.TED_ED -> VideoType.INSTRUCTIONAL
            LegacyVideoType.SHORT_PROGRAMME -> VideoType.INSTRUCTIONAL

            LegacyVideoType.NEWS -> VideoType.NEWS
            LegacyVideoType.UGC_NEWS -> VideoType.NEWS
            LegacyVideoType.NEWS_PACKAGE -> VideoType.NEWS

            LegacyVideoType.STOCK -> VideoType.STOCK
            LegacyVideoType.VR_360_STOCK -> VideoType.STOCK
            LegacyVideoType.VR_360_IMMERSIVE -> VideoType.STOCK
            LegacyVideoType.OTHER -> VideoType.STOCK
            LegacyVideoType.TV_CLIPS -> VideoType.STOCK
        }
    }
}
