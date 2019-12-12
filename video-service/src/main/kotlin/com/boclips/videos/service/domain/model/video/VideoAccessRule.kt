package com.boclips.videos.service.domain.model.video

sealed class VideoAccessRule {
    data class SpecificIds(val videoIds: Set<VideoId>) : VideoAccessRule()
    object Everything : VideoAccessRule()
}