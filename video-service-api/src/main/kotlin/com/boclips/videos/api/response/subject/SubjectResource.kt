package com.boclips.videos.api.response.subject

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

data class SubjectResource(
    val id: String,
    val name: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
