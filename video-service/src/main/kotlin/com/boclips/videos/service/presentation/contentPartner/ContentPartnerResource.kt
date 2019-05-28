package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "contentPartners")
open class ContentPartnerResource(
    val name: String,
    val ageRange: AgeRangeResource? = null
)
