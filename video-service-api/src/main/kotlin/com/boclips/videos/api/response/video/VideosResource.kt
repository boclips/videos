package com.boclips.videos.api.response.video

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.PagedModel

class VideosResource(
    var _embedded: VideosWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var page: PagedModel.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)

data class VideosWrapperResource(
    val videos: List<VideoResource>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val facets: VideoFacetsResource?
)
