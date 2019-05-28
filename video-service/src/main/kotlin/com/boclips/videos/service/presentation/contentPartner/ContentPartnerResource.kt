package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeResource

open class ContentPartnerResource(
    val name: String,
    val ageRange: AgeRangeResource? = null
)
