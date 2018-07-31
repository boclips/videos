package com.boclips.videoanalyser.domain.duplicates.model

import com.boclips.videoanalyser.domain.model.BoclipsVideo

data class Duplicate(
        val originalVideo: BoclipsVideo,
        val duplicates: List<BoclipsVideo>
)