package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.common.Specifiable

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
    val logos: List<String>? = null,
    val showreel: Specifiable<String>? = null,
    val sampleVideos: List<String>? = null
)

