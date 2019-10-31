package com.boclips.search.service.infrastructure.videos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class VideoDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(TITLE) val title: String,
    @param:JsonProperty(DESCRIPTION) val description: String,
    @param:JsonProperty(CONTENT_PROVIDER) val contentProvider: String,
    @param:JsonProperty(RELEASE_DATE) val releaseDate: LocalDate?,
    @param:JsonProperty(KEYWORDS) val keywords: List<String>,
    @param:JsonProperty(TAGS) val tags: List<String>,
    @param:JsonProperty(DURATION_SECONDS) val durationSeconds: Long?,
    @param:JsonProperty(SOURCE) val source: String?,
    @param:JsonProperty(TRANSCRIPT) val transcript: String?,
    @param:JsonProperty(AGE_RANGE_MIN) val ageRangeMin: Int?,
    @param:JsonProperty(AGE_RANGE_MAX) val ageRangeMax: Int?,
    @param:JsonProperty(TYPE) val type: String?,
    @param:JsonProperty(SUBJECT_IDS) val subjectIds: Set<String>?,
    @param:JsonProperty(SUBJECT_NAMES) val subjectNames: Set<String>?,
    @param:JsonProperty(PROMOTED) val promoted: Boolean?
) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CONTENT_PROVIDER = "contentProvider"
        const val RELEASE_DATE = "releaseDate"
        const val KEYWORDS = "keywords"
        const val TAGS = "tags"
        const val DURATION_SECONDS = "durationSeconds"
        const val SOURCE = "source"
        const val TRANSCRIPT = "transcript"
        const val AGE_RANGE_MIN = "ageRangeMin"
        const val AGE_RANGE_MAX = "ageRangeMax"
        const val SUBJECT_IDS = "subjectIds"
        const val SUBJECT_NAMES = "subjectNames"
        const val TYPE = "type"
        const val PROMOTED = "promoted"
    }
}
