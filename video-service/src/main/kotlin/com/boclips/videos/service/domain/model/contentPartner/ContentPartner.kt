package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange?
)