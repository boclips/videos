package com.boclips.videos.api.response.contentpartner


import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "whatToExpect")
open class WhatToExpectResource(
    var _embedded: WhatToExpectWrapperResource
 )

data class WhatToExpectWrapperResource(
    val whatToExpect:  List<String>
)