package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.common.IngestType
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class ContentPartnerFilterRequest(
    val name: String? = null,
    val official: Boolean? = null,
    val accreditedToYtChannelId: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val ingestType: List<IngestType>? = null
)
