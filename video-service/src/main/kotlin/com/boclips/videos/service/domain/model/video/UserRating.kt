package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.UserId

data class UserRating(
    val rating: Int,
    val userId: UserId
)