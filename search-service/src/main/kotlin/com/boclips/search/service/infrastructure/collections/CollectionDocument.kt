package com.boclips.search.service.infrastructure.collections

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String,
    @param:JsonProperty(VISIBILITY) val visibility: String?,
    @param:JsonProperty(SUBJECTS) val subjects: List<String> = emptyList(),
    @param:JsonProperty(HAS_ATTACHMENTS) val hasAttachments: Boolean,
    @param:JsonProperty(OWNER) val owner: String
) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val VISIBILITY = "visibility"
        const val SUBJECTS = "subjects"
        const val HAS_ATTACHMENTS = "hasAttachments"
        const val OWNER = "owner"
    }
}
