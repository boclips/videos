package com.boclips.videos.api.response.video

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedResources

class VideosResource(
    var _embedded: VideosWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var page: PagedResources.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class VideosWrapperResource(
    val videos: List<VideoResource>
)
