package com.boclips.videos.api.response.contentpartner

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

class EduAgeRangesResource(
    var _embedded: EduAgeRangesWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class EduAgeRangesWrapperResource(
    val ageRanges: List<EduAgeRangeResource>
)
