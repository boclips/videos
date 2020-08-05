package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.search.service.domain.videos.model.VoiceType as SearchVoiceType
import com.boclips.videos.service.domain.model.video.VoiceType as VideoVoiceType

object VideoAccessRuleConverter {
    fun mapToPermittedVideoIds(videoAccess: VideoAccess): Set<String>? {
        return when (videoAccess) {
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedIds>()
                .takeIf { it.isNotEmpty() }
                ?.flatMap { accessRule -> accessRule.videoIds.map { id -> id.value } }
                ?.toSet()
            VideoAccess.Everything -> null
        }
    }

    fun mapToDeniedVideoIds(videoAccess: VideoAccess): Set<String>? {
        return when (videoAccess) {
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedIds>()
                .takeIf { it.isNotEmpty() }
                ?.flatMap { accessRule -> accessRule.videoIds.map { id -> id.value } }
                ?.toSet()
            VideoAccess.Everything -> null
        }
    }

    fun mapToExcludedVideoTypes(videoAccess: VideoAccess): Set<VideoType> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedContentTypes>()
                .flatMap { accessRule -> accessRule.contentTypes.map { SearchQueryConverter().convertType(it.name) } }
                .toSet()
        }

    fun mapToIncludedChannelIds(videoAccess: VideoAccess): Set<String> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedChannelIds>()
                .flatMap { accessRule -> accessRule.channelIds.map { it.value } }
                .toSet()
        }

    fun mapToExcludedChannelIds(videoAccess: VideoAccess): Set<String> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedChannelIds>()
                .flatMap { accessRule -> accessRule.channelIds.map { id -> id.value } }
                .toSet()
        }

    fun isEligibleForStreaming(videoAccess: VideoAccess): Boolean? =
        when (videoAccess) {
            VideoAccess.Everything -> null
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedDistributionMethods>()
                .takeUnless { it.isEmpty() }
                ?.flatMap { accessRule -> accessRule.distributionMethods }
                ?.contains(DistributionMethod.STREAM)
        }

    fun mapToIncludedVoiceTypes(videoAccess: VideoAccess): Set<SearchVoiceType> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedVideoVoiceTypes>()
                .flatMap { accessRule ->
                    accessRule.voiceTypes.map { voiceType ->
                        when (voiceType) {
                            VideoVoiceType.WITH_VOICE -> SearchVoiceType.WITH
                            VideoVoiceType.WITHOUT_VOICE -> SearchVoiceType.WITHOUT
                            VideoVoiceType.UNKNOWN -> SearchVoiceType.UNKNOWN
                        }
                    }
                }.toSet()
        }
}
