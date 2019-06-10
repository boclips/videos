package com.boclips.videos.service.infrastructure.contentPartner

data class ContentPartnerUpdateCommand(
    val youtubeChannelId: String? = null,
    val name: String? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null
)

