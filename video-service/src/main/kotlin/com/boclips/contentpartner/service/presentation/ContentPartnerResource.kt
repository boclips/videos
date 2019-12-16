package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.LegalRestrictionsResource
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeResource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "contentPartners")
open class ContentPartnerResource(
    val id: String,
    val name: String,
    val ageRange: AgeRangeResource? = null,
    val official: Boolean,
    val legalRestrictions: LegalRestrictionsResource? = null,
    val distributionMethods: Set<DistributionMethodResource>,
    val currency: String?
)
