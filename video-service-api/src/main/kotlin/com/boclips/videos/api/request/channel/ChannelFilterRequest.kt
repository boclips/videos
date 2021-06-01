package com.boclips.videos.api.request.channel

import com.boclips.videos.api.request.Projection
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

enum class SortByRequest {
    CATEGORIES_ASC,
    CATEGORIES_DESC,
    NAME_ASC,
    NAME_DESC
}

data class ChannelFilterRequest(
    val name: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val ingestType: List<String>? = null,
    val projection: Projection? = null,
    val sort_by: SortByRequest? = null,
    val page: Int? = null,
    val size: Int? = null
)
