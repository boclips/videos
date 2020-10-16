package com.boclips.search.service.infrastructure.subjects

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SubjectDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(NAME) val name: String,
) {
    companion object {
        const val ID = "id"
        const val NAME = "name"
    }
}