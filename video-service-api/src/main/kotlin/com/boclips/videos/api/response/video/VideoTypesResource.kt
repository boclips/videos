package com.boclips.videos.api.response.video

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

open class VideoTypesResource(
    var _embedded: VideoTypesWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class VideoTypesWrapperResource(
    val videoTypes: List<String>
)
