package com.boclips.videos.service.domain.model.tag

import com.boclips.videos.service.domain.model.common.UserId

data class UserTag(
    val tag: Tag,
    val userId: UserId
)
