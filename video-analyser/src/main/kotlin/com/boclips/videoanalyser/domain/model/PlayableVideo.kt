package com.boclips.videoanalyser.domain.model

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo

class PlayableVideo(
        val boclipsVideo: BoclipsVideo,
        val kalturaVideo: KalturaVideo
)