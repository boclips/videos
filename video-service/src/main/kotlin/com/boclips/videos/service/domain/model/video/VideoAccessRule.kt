package com.boclips.videos.service.domain.model.video

sealed class VideoAccessRule {
    data class ExcludedIds(val videoIds: Set<VideoId>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${videoIds.size} videos)"
        }
    }

    data class IncludedIds(val videoIds: Set<VideoId>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to include ${videoIds.size} videos)"
        }
    }

    data class ExcludedContentTypes(val contentTypes: Set<ContentType>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${contentTypes.size} content types)"
        }
    }

    data class ExcludedContentPartners(val contentPartnerIds: Set<ContentPartnerId>): VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${contentPartnerIds.size} content partners)"
        }
    }
}

sealed class VideoAccess {
    object Everything : VideoAccess() {
        override fun toString(): String {
            return "Everything - VideoAccessRule"
        }
    }

    class Rules(val accessRules: List<VideoAccessRule>) : VideoAccess()
}