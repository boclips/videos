package com.boclips.videos.service.presentation.deliveryMethod

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "distributionMethods")
enum class DistributionMethodResource {
    DOWNLOAD,
    STREAM
}