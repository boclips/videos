package com.boclips.videoanalyser.domain.duplicates.model

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo

data class Duplicate(
        val originalVideo: BoclipsVideo,
        val duplicates: List<BoclipsVideo>
)