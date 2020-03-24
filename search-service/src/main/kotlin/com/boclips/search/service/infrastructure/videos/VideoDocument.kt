package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.common.HasAgeRange
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class VideoDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String,
    @param:JsonProperty(DESCRIPTION) val description: String,
    @param:JsonProperty(CONTENT_PROVIDER) val contentProvider: String,
    @param:JsonProperty(CONTENT_PARTNER_ID) val contentPartnerId: String?,
    @param:JsonProperty(RELEASE_DATE) val releaseDate: LocalDate?,
    @param:JsonProperty(KEYWORDS) val keywords: List<String>,
    @param:JsonProperty(TAGS) val tags: List<String>,
    @param:JsonProperty(IS_CLASSROOM) val isClassroom: Boolean?,
    @param:JsonProperty(DURATION_SECONDS) val durationSeconds: Long?,
    @param:JsonProperty(SOURCE) val source: String?,
    @param:JsonProperty(TRANSCRIPT) val transcript: String?,
    @param:JsonProperty(HasAgeRange.AGE_RANGE_MIN) override val ageRangeMin: Int?,
    @param:JsonProperty(HasAgeRange.AGE_RANGE_MAX) override val ageRangeMax: Int?,
    @param:JsonProperty(HasAgeRange.AGE_RANGE) override val ageRange: List<Int>?,
    @param:JsonProperty(TYPE) val type: String?,
    @param:JsonProperty(SUBJECT_IDS) val subjectIds: Set<String>?,
    @param:JsonProperty(SUBJECT_NAMES) val subjectNames: Set<String>?,
    @param:JsonProperty(PROMOTED) val promoted: Boolean?,
    @param:JsonProperty(MEAN_RATING) val meanRating: Double?,
    @param:JsonProperty(SUBJECTS_SET_MANUALLY) val subjectsSetManually: Boolean?,
    @param:JsonProperty(ELIGIBLE_FOR_STREAM) val eligibleForStream: Boolean? = true, //TODO make these non-optional after re-index
    @param:JsonProperty(ELIGIBLE_FOR_DOWNLOAD) val eligibleForDownload: Boolean?
) : HasAgeRange {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CONTENT_PROVIDER = "contentProvider"
        const val CONTENT_PARTNER_ID = "contentPartnerId"
        const val RELEASE_DATE = "releaseDate"
        const val KEYWORDS = "keywords"
        const val TAGS = "tags"
        const val IS_CLASSROOM = "isClassroom"
        const val DURATION_SECONDS = "durationSeconds"
        const val SOURCE = "source"
        const val TRANSCRIPT = "transcript"
        const val AGE_RANGE = "ageRange"
        const val SUBJECT_IDS = "subjectIds"
        const val SUBJECT_NAMES = "subjectNames"
        const val TYPE = "type"
        const val PROMOTED = "promoted"
        const val MEAN_RATING = "meanRating"
        const val SUBJECTS_SET_MANUALLY = "subjectsSetManually"
        const val ELIGIBLE_FOR_STREAM = "eligibleForStream"
        const val ELIGIBLE_FOR_DOWNLOAD = "eligibleForDownload"
    }
}
