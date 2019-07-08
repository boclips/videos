package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import javax.validation.Valid

data class ContentPartnerRequest(
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    @field:Valid var ageRange: AgeRangeRequest? = null,

    val searchable: Boolean? = null,

    val hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource>? = null
)
