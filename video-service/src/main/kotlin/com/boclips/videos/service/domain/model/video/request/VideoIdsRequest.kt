package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoId

class VideoIdsRequest(val ids: List<VideoId>) {
    fun toSearchQuery(): VideoQuery {
        return VideoQuery(
            ids = ids.map { it.value }.toSet()
        )
    }
}

