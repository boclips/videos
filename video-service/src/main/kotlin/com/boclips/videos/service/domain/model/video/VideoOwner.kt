package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId

data class VideoOwner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val videoReference: String
)
