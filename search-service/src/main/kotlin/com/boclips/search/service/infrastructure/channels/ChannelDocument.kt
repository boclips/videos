package com.boclips.search.service.infrastructure.channels

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ChannelDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(NAME) val name: String,
) {
    companion object {
        const val ID = "id"
        const val NAME = "name"
    }
}