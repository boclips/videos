package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoId

class VideoIdsRequest(val ids: List<VideoId>) {
    fun toSearchQuery(videoAccess: VideoAccess): VideoQuery {
        return VideoQuery(
            userQuery = UserQuery(ids = ids.map { it.value }.toSet()),
            videoAccessRuleQuery = AccessRuleQueryConverter.toVideoAccessRuleQuery(videoAccess)
        )
    }
}

