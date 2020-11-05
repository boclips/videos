package com.boclips.videos.api.response.video

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

data class VideoIdsResource(
    val _embedded: VideoIdsWrapper,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val _links: Map<String, HateoasLink>?
)

data class VideoIdsWrapper(
    val videoIds: List<String>
)
