package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.video.VideoAccessRuleConverter

object AccessRuleQueryConverter {
    fun fromAccessRules(videoAccess: VideoAccess): AccessRuleQuery {
        return AccessRuleQuery(
            permittedVideoIds = VideoAccessRuleConverter.mapToPermittedVideoIds(videoAccess),
            deniedVideoIds = VideoAccessRuleConverter.mapToDeniedVideoIds(videoAccess),
            excludedTypes = VideoAccessRuleConverter.mapToExcludedVideoTypes(videoAccess),
            includedTypes = VideoAccessRuleConverter.mapToIncludedVideoTypes(videoAccess),
            excludedContentPartnerIds = VideoAccessRuleConverter.mapToExcludedChannelIds(videoAccess),
            includedChannelIds = VideoAccessRuleConverter.mapToIncludedChannelIds(videoAccess),
            isEligibleForStream = VideoAccessRuleConverter.isEligibleForStreaming(videoAccess),
            includedVoiceType = VideoAccessRuleConverter.mapToIncludedVoiceTypes(videoAccess)
        )
    }
}
