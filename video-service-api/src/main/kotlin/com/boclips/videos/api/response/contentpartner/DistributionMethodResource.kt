package com.boclips.videos.api.response.contentpartner

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "distributionMethods")
enum class DistributionMethodResource {
    DOWNLOAD,
    STREAM
}