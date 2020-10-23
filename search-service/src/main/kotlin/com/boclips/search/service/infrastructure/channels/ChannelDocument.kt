package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ContentType
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ChannelDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(NAME) val name: String,
    @param:JsonProperty(TYPES) val types: List<ContentType>,
    @param:JsonProperty(ELIGIBLE_FOR_STREAM) val eligibleForStream: Boolean,
    ) {
    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val TYPES = "types"
        const val ELIGIBLE_FOR_STREAM = "eligibleForStream"
    }
}