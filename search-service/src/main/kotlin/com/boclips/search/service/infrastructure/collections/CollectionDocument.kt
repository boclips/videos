package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.common.HasAgeRange
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class CollectionDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String,
    @param:JsonProperty(SEARCHABLE) val searchable: Boolean? = false,
    @param:JsonProperty(SUBJECTS) val subjects: List<String> = emptyList(),
    @param:JsonProperty(BOOKMARKED_BY) val bookmarkedBy: Set<String> = emptySet(),
    @param:JsonProperty(HAS_ATTACHMENTS) val hasAttachments: Boolean? = false,
    @param:JsonProperty(OWNER) val owner: String?,
    @param:JsonProperty(DESCRIPTION) val description: String?,
    @param:JsonProperty(HAS_LESSON_PLANS) val hasLessonPlans: Boolean? = false,
    @param:JsonProperty(PROMOTED) val promoted: Boolean? = false,
    @param:JsonProperty(DEFAULT) val default: Boolean? = false,
    @param:JsonProperty(LAST_MODIFIED) val lastModified: ZonedDateTime?,
    @param:JsonProperty(ATTACHMENT_TYPES) val attachmentTypes: Set<String>?,
    @param:JsonProperty(HasAgeRange.AGE_RANGE_MIN) override val ageRangeMin: Int?,
    @param:JsonProperty(HasAgeRange.AGE_RANGE_MAX) override val ageRangeMax: Int?,
    @param:JsonProperty(HasAgeRange.AGE_RANGE) override val ageRange: List<Int>?
) : HasAgeRange {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val VISIBILITY = "visibility"
        const val SEARCHABLE = "searchable"
        const val SUBJECTS = "subjects"
        const val HAS_ATTACHMENTS = "hasAttachments"
        const val OWNER = "owner"
        const val BOOKMARKED_BY = "bookmarkedBy"
        const val DESCRIPTION = "description"
        const val HAS_LESSON_PLANS = "hasLessonPlans"
        const val PROMOTED = "promoted"
        const val DEFAULT = "default"
        const val LAST_MODIFIED = "lastModified"
        const val ATTACHMENT_TYPES = "attachmentTypes"
    }
}
