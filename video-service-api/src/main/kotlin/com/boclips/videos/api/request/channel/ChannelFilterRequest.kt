package com.boclips.videos.api.request.channel

import com.boclips.videos.api.request.Projection
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class ChannelFilterRequest(
    val name: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val ingestType: List<String>? = null,
    val projection: Projection? = null
)
