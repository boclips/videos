package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource

data class BulkUpdateRequest(
    val ids: List<String>,
    val distributionMethods: Set<DistributionMethodResource>? = null
)
