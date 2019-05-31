package com.boclips.search.service.infrastructure.collections

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ESCollection @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String

) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
    }
}
