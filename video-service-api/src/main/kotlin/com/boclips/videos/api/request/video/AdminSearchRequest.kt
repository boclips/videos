package com.boclips.videos.api.request.video

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class AdminSearchRequest(
    @JsonSetter(contentNulls = Nulls.FAIL)
    val ids: List<String>?,
    val contentPartnerId: String?
)
