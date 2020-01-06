package com.boclips.videos.api.response.subject

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

class SubjectsResource(
    var _embedded: SubjectsWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>?
)

data class SubjectsWrapperResource(
    val subjects: List<SubjectResource>
)
