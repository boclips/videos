package com.boclips.videos.service.domain.model.video

sealed class VideoAccessRule {
    data class SpecificIds(val videoIds: Set<VideoId>) : VideoAccessRule() {
        override fun toString(): String {
            return "VideoAccessRule(restricted to ${videoIds.size} videos)"
        }
    }
    object Everything : VideoAccessRule() {
        override fun toString(): String {
            return "Everything - VideoAccessRule"
        }
    }
}
