package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.channels.model.SuggestionAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.video.AccessRuleConverter

object AccessRuleQueryConverter {
    fun toVideoAccessRuleQuery(videoAccess: VideoAccess): VideoAccessRuleQuery {
        return VideoAccessRuleQuery(
            permittedVideoIds = AccessRuleConverter.mapToPermittedVideoIds(videoAccess),
            deniedVideoIds = AccessRuleConverter.mapToDeniedVideoIds(videoAccess),
            excludedTypes = AccessRuleConverter.mapToExcludedVideoTypes(videoAccess),
            includedTypes = AccessRuleConverter.mapToIncludedVideoTypes(videoAccess),
            excludedContentPartnerIds = AccessRuleConverter.mapToExcludedChannelIds(videoAccess),
            includedChannelIds = AccessRuleConverter.mapToIncludedChannelIds(videoAccess),
            isEligibleForStream = AccessRuleConverter.isEligibleForStreaming(videoAccess),
            isEligibleForDownload = AccessRuleConverter.isEligibleForDownload(videoAccess),
            includedVoiceType = AccessRuleConverter.mapToIncludedVoiceTypes(videoAccess),
            excludedLanguages = AccessRuleConverter.mapToExcludedLanguages(videoAccess)
        )
    }

    fun toSuggestionAccessRuleQuery(videoAccess: VideoAccess): SuggestionAccessRuleQuery {
        return SuggestionAccessRuleQuery(
            excludedTypes = AccessRuleConverter.mapToExcludedContentTypes(videoAccess),
            includedTypes = AccessRuleConverter.mapToIncludedContentTypes(videoAccess),
            excludedContentPartnerIds = AccessRuleConverter.mapToExcludedChannelIds(videoAccess),
            includedChannelIds = AccessRuleConverter.mapToIncludedChannelIds(videoAccess),
            isEligibleForStream = AccessRuleConverter.isEligibleForStreaming(videoAccess),
        )
    }
}
