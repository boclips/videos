package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import javax.validation.Valid
import javax.validation.constraints.NotBlank

data class ContentPartnerRequest(
    @field:NotBlank
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    @field:Valid var ageRange: AgeRangeRequest? = null,
    val distributionMethods: Set<DistributionMethodResource>? = null
)
