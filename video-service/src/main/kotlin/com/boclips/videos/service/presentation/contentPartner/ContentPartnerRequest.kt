package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class ContentPartnerRequest(
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    @field:Valid var ageRange: AgeRangeRequest? = null,

    @field:NotNull
    val searchable: Boolean? = null
)
