package com.boclips.videos.api.request.channel

import com.boclips.videos.api.common.Specifiable
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

enum class ChannelStatusRequest {
    NEEDS_INTRODUCTION,
    HAVE_REACHED_OUT,
    NEEDS_CONTENT,
    WAITING_FOR_INGEST,
    SHOULD_ADD_TO_SITE,
    SHOULD_PROMOTE,
    PROMOTED
}

data class MarketingInformationRequest(
    val status: ChannelStatusRequest? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val logos: List<String>? = null,
    val showreel: Specifiable<String>? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val sampleVideos: List<String>? = null
)
