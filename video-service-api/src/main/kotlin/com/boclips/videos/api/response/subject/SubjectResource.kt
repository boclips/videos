package com.boclips.videos.api.response.subject

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class SubjectResource(
    val id: String,
    val name: String? = null,
    val lessonPlan: Boolean? = false,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)