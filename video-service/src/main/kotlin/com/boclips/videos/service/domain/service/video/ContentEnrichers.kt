package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video

class ContentEnrichers {
    companion object {
        fun isNews(video: Video): Boolean {
            return video.type == ContentType.NEWS
        }
    }
}
