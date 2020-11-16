package com.boclips.videos.api.response.video

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

class VideoIdsResource(
    var _embedded: VideoIdsWrapper,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>?
)

data class VideoIdsWrapper(
    val videoIds: List<String>
)
