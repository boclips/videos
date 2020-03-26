package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.UserId

data class UserRating(
    val rating: Int,
    val userId: UserId
)
