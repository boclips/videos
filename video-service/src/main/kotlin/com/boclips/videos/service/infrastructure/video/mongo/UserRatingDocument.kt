package com.boclips.videos.service.infrastructure.video.mongo

data class UserRatingDocument(
    val rating: Int,
    val userId: String
)