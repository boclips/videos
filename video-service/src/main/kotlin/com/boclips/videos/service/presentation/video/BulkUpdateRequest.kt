package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource

data class BulkUpdateRequest(
    val ids: List<String>,

    val hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource>? = null
)
