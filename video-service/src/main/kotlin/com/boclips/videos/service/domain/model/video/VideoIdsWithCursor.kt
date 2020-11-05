package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.PagingCursor

data class VideoIdsWithCursor(
        val videoIds: List<VideoId>,
        val cursor: PagingCursor? = null
    )
