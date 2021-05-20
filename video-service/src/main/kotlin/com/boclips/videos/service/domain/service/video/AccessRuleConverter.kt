package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.application.common.QueryConverter
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import java.util.Locale
import com.boclips.search.service.domain.videos.model.VoiceType as SearchVoiceType
import com.boclips.videos.service.domain.model.video.VoiceType as VideoVoiceType

object AccessRuleConverter {
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
                .flatMap { accessRule -> accessRule.contentTypes.map { QueryConverter().convertTypeToVideoType(it.name) } }
                .toSet()
        }

    fun mapToExcludedContentTypes(videoAccess: VideoAccess): Set<ContentType> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedContentTypes>()
                .flatMap { accessRule -> accessRule.contentTypes.map { QueryConverter().convertTypeToContentType(it.name) } }
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
            is VideoAccess.Rules -> {
                videoAccess.accessRules
                    .filterIsInstance<VideoAccessRule.IncludedDistributionMethods>()
                    .takeUnless { it.isEmpty() }
                    ?.flatMap { accessRule -> accessRule.distributionMethods }
                    ?.let { distributionMethods: List<DistributionMethod> ->
                        when {
                            distributionMethods.contains(DistributionMethod.STREAM) -> true
                            else -> null
                        }
                    }
            }
        }

    fun isEligibleForDownload(videoAccess: VideoAccess): Boolean? =
        when (videoAccess) {
            VideoAccess.Everything -> null
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedDistributionMethods>()
                .takeUnless { it.isEmpty() }
                ?.flatMap { accessRule -> accessRule.distributionMethods }
                ?.let { distributionMethods: List<DistributionMethod> ->
                    when {
                        distributionMethods.contains(DistributionMethod.DOWNLOAD) -> true
                        else -> null
                    }
                }
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

    fun mapToIncludedVideoTypes(videoAccess: VideoAccess): Set<VideoType> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedContentTypes>()
                .flatMap { accessRule -> accessRule.contentTypes.map { QueryConverter().convertTypeToVideoType(it.name) } }
                .toSet()
        }

    fun mapToIncludedContentTypes(videoAccess: VideoAccess): Set<ContentType> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.IncludedContentTypes>()
                .flatMap { accessRule -> accessRule.contentTypes.map { QueryConverter().convertTypeToContentType(it.name) } }
                .toSet()
        }

    fun mapToExcludedLanguages(videoAccess: VideoAccess): Set<Locale> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedLanguages>()
                .flatMap { accessRule -> accessRule.languages }
                .toSet()
        }

    fun mapToExcludedSourceTypes(videoAccess: VideoAccess): Set<SourceType> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedPlaybackProviderTypes>()
                .flatMap { accessRule -> accessRule.sources }.map { QueryConverter().convertToSourceType(it) }
                .toSet()
        }
}
