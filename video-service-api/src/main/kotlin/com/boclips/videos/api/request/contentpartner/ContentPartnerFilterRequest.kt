package com.boclips.videos.api.request.contentpartner

data class ContentPartnerFilterRequest(
    val name: String? = null,
    val official: Boolean? = null,
    val accreditedToYtChannelId: String? = null,
    val ingestType: List<String>? = null
)
