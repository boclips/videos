package com.boclips.videos.api.response.tag

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class TagResource(
    val id: String,
    val label: String? = null,
    val userId: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)
