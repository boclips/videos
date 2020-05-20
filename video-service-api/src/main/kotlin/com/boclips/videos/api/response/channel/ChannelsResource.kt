package com.boclips.videos.api.response.channel

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

open class ChannelsResource(
    var _embedded: ChannelWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class ChannelWrapperResource(
    val channels: List<ChannelResource>
)
