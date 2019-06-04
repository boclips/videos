package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner

data class VideoOwner(
    val contentPartner: ContentPartner,
    val videoReference: String
)
