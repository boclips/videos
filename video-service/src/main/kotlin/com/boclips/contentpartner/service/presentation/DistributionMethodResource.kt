package com.boclips.contentpartner.service.presentation

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "distributionMethods")
enum class DistributionMethodResource {
    DOWNLOAD,
    STREAM
}