package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.common.Specifiable
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

enum class ContentPartnerStatusRequest {
    NEEDS_INTRODUCTION,
    HAVE_REACHED_OUT,
    NEEDS_CONTENT,
    WAITING_FOR_INGEST,
    SHOULD_ADD_TO_SITE,
    SHOULD_PROMOTE,
    PROMOTED
}

data class ContentPartnerMarketingInformationRequest(
    val status: ContentPartnerStatusRequest? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val logos: List<String>? = null,
    val showreel: Specifiable<String>? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val sampleVideos: List<String>? = null
)

