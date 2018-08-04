package com.boclips.videoanalyser.domain.model

data class DuplicateVideo(
        val originalVideo: BoclipsVideo,
        val duplicates: List<BoclipsVideo>
)