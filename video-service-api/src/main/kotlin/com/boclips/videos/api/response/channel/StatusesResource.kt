package com.boclips.videos.api.response.channel

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

open class StatusesResource(
    var _embedded: StatusWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class StatusWrapperResource(
    val statuses: List<String>
)
