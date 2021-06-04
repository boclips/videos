package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import java.util.Locale

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

    data class ExcludedContentTypes(val contentTypes: Set<VideoType>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${contentTypes.size} content types)"
        }
    }

    data class IncludedContentTypes(val contentTypes: Set<VideoType>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to include ${contentTypes.size} content types)"
        }
    }

    data class ExcludedChannelIds(val channelIds: Set<ChannelId>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${channelIds.size} channels)"
        }
    }

    data class IncludedChannelIds(val channelIds: Set<ChannelId>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to include ${channelIds.size} channels)"
        }
    }

    data class IncludedHiddenChannelIds(val channelIds: Set<ChannelId>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(includes hidden ${channelIds.size} channels)"
        }
    }

    data class IncludedDistributionMethods(val distributionMethods: Set<DistributionMethod>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to included $distributionMethods distribution methods)"
        }
    }

    data class IncludedVideoVoiceTypes(val voiceTypes: Set<VoiceType>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to included ${voiceTypes.joinToString { it.name }} voiced content)"
        }
    }

    data class ExcludedLanguages(val languages: Set<Locale>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${languages.joinToString { it.displayLanguage }} content)"
        }
    }

    data class ExcludedPlaybackProviderTypes(val sources: Set<PlaybackProviderType>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to exclude ${sources.joinToString { it.name }} content)"
        }
    }
}

sealed class VideoAccess {
    object Everything : VideoAccess() {
        override fun toString(): String {
            return "Everything - VideoAccessRule"
        }
    }

    data class Rules(val accessRules: List<VideoAccessRule>) : VideoAccess() {
        override fun toString(): String {
            return accessRules.joinToString { it.toString() }
        }
    }
}
