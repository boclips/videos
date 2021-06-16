package com.boclips.videos.api.response.contentwarning

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class ContentWarningsResource(
    val _embedded: ContentWarningWrapper,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>?
)

data class ContentWarningWrapper(
    val contentWarnings: List<ContentWarningResource>
)
