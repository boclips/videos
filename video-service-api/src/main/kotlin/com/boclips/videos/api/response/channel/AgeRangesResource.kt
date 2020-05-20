package com.boclips.videos.api.response.channel

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

class AgeRangesResource(
    var _embedded: AgeRangesWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class AgeRangesWrapperResource(
    val ageRanges: List<AgeRangeResource>
)
