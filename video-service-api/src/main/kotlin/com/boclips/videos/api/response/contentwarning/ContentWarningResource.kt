package com.boclips.videos.api.response.contentwarning

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class ContentWarningResource(
    val id: String,
    val label: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)
