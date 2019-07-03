package com.boclips.videos.service.presentation.video

data class BulkUpdateRequest(
    val ids: List<String>,
    val status: VideoResourceStatus?,
    val hiddenFromSearchForDeliveryMethods: Set<VideoResourceDeliveryMethod>? = null
)