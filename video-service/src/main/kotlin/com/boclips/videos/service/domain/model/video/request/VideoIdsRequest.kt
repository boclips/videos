package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoAccessRuleConverter

class VideoIdsRequest(
    val ids: List<VideoId>
) {
    fun toSearchQuery(videoAccess: VideoAccess): VideoQuery {
        return VideoQuery(
            ids = ids.map { it.value },
            permittedVideoIds = VideoAccessRuleConverter.mapToPermittedVideoIds(
                videoAccess
            ),
            deniedVideoIds = VideoAccessRuleConverter.mapToDeniedVideoIds(
                videoAccess
            ),
            excludedType = VideoAccessRuleConverter.mapToExcludedVideoTypes(
                videoAccess
            ),
            excludedContentPartnerIds = VideoAccessRuleConverter.mapToExcludedContentPartnerIds(
                videoAccess
            )
        )
    }
}