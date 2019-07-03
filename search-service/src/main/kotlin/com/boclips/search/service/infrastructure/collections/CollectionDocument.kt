package com.boclips.search.service.infrastructure.collections

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String,
    @param:JsonProperty(SUBJECTS) val subjects: List<String> = emptyList()
) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val SUBJECTS = "subjects"
    }
}
