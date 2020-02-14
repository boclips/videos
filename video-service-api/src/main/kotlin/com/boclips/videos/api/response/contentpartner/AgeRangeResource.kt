package com.boclips.videos.api.response.contentpartner

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

data class AgeRangeResource(
    val id: String,
    val label: String?,
    val min: Int?,
    val max: Int?,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
