package com.boclips.videos.api.response.subject

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

class SubjectsResource(
    var _embedded: SubjectsWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>?
)

data class SubjectsWrapperResource(
    val subjects: List<SubjectResource>
)
