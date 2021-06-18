package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat

interface CaptionConverter {
    fun convert(content: String, from: CaptionFormat, to: CaptionFormat): String
    fun convertToTranscript(caption: Caption): String
}
