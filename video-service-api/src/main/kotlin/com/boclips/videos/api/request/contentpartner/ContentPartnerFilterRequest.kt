package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.common.IngestType

data class ContentPartnerFilterRequest(
    val name: String? = null,
    val official: Boolean? = null,
    val accreditedToYtChannelId: String? = null,
    val ingestType: List<IngestType>? = null
)
