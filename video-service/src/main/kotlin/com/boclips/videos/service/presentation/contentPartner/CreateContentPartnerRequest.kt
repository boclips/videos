package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import javax.validation.Valid

data class CreateContentPartnerRequest(
    val name: String,

    @field:Valid
    var ageRange: AgeRangeRequest? = null
    )
