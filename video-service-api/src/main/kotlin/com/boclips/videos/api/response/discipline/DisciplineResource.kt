package com.boclips.videos.api.response.discipline

import com.boclips.videos.api.response.subject.SubjectResource
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class DisciplineResource constructor(
    val id: String,
    val name: String? = null,
    val code: String? = null,
    val subjects: List<SubjectResource>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>?
)
