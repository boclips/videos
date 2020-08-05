package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.video.VideoAccessRuleConverter

object VideoQueryEnricher {
    fun enrichFromAccessRules(videoQuery: VideoQuery, videoAccess: VideoAccess): VideoQuery {
        return videoQuery.copy(
            permittedVideoIds = VideoAccessRuleConverter.mapToPermittedVideoIds(videoAccess),
            deniedVideoIds = VideoAccessRuleConverter.mapToDeniedVideoIds(videoAccess),
            excludedTypes = VideoAccessRuleConverter.mapToExcludedVideoTypes(videoAccess),

            // included types can also be specified by the user, maybe user filters and access rule filters should be split
            includedTypes = videoQuery.includedTypes + VideoAccessRuleConverter.mapToIncludedVideoTypes(videoAccess),
            excludedContentPartnerIds = VideoAccessRuleConverter.mapToExcludedChannelIds(videoAccess),
            includedChannelIds = VideoAccessRuleConverter.mapToIncludedChannelIds(videoAccess),
            isEligibleForStream = VideoAccessRuleConverter.isEligibleForStreaming(videoAccess),
            includedVoiceType = VideoAccessRuleConverter.mapToIncludedVoiceTypes(videoAccess)
        )
    }
}
