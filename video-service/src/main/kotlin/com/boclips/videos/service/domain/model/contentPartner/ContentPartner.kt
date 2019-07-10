package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.DeliveryMethod

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange,
    val credit: Credit,
    val hiddenFromSearchForDeliveryMethods: Set<DeliveryMethod>
)
