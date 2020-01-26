package com.boclips.videos.api.response.tag

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class TagsResource(
    val _embedded: TagsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>?
)

data class TagsWrapperResource(
    val tags: List<TagResource>
)
