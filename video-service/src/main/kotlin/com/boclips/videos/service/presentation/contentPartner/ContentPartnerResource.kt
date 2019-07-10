package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "contentPartners")
open class ContentPartnerResource(
    val id: String,
    val name: String,
    val ageRange: AgeRangeResource? = null,
    val isOfficial: Boolean,
    val hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource>
)
