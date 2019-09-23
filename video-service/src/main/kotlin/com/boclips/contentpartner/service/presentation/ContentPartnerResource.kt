package com.boclips.contentpartner.service.presentation

import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "contentPartners")
open class ContentPartnerResource(
    val id: String,
    val name: String,
    val ageRange: AgeRangeResource? = null,
    val official: Boolean,
    val distributionMethods: Set<DistributionMethodResource>,
    val currency: String?
)
