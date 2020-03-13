package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule

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

    fun mapToExcludedContentPartnerIds(videoAccess: VideoAccess): Set<String> =
        when (videoAccess) {
            VideoAccess.Everything -> emptySet()
            is VideoAccess.Rules -> videoAccess.accessRules
                .filterIsInstance<VideoAccessRule.ExcludedContentPartners>()
                .flatMap { accessRule -> accessRule.contentPartnerIds.map { id -> id.value } }
                .toSet()
        }
}