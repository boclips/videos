package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.Video

class ContentEnrichers {
    companion object {
        fun isNews(video: BaseVideo): Boolean {
            return video.types.contains(VideoType.NEWS)
        }
    }
}
