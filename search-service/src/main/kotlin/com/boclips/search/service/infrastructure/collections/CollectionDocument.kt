package com.boclips.search.service.infrastructure.collections

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String,
    @param:JsonProperty(VISIBILITY) val visibility: String?,
    @param:JsonProperty(SUBJECTS) val subjects: List<String> = emptyList(),
    @param:JsonProperty(BOOKMARKED_BY) val bookmarkedBy: Set<String> = emptySet(),
    @param:JsonProperty(HAS_ATTACHMENTS) val hasAttachments: Boolean?,
    @param:JsonProperty(OWNER) val owner: String?,
    @param:JsonProperty(DESCRIPTION) val description: String?,
    @param:JsonProperty(HAS_LESSON_PLANS) val hasLessonPlans: Boolean?,
    @param:JsonProperty(AGE_RANGE_MIN) val ageRangeMin: Int?,
    @param:JsonProperty(AGE_RANGE_MAX) val ageRangeMax: Int?

) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val VISIBILITY = "visibility"
        const val SUBJECTS = "subjects"
        const val HAS_ATTACHMENTS = "hasAttachments"
        const val OWNER = "owner"
        const val BOOKMARKED_BY = "bookmarkedBy"
        const val DESCRIPTION = "description"
        const val HAS_LESSON_PLANS = "hasLessonPlans"
        const val AGE_RANGE_MIN = "ageRangeMin"
        const val AGE_RANGE_MAX = "ageRangeMax"
    }
}
