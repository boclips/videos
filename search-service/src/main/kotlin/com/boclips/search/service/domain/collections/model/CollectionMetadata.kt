package com.boclips.search.service.domain.collections.model

data class CollectionMetadata(
    val id: String,
    val title: String,
    val subjectIds: List<String>,
    val visibility: CollectionVisibility,
    val owner: String,
    val bookmarkedByUsers: Set<String>,
    val hasAttachments: Boolean,
    val description: String?,
    val hasLessonPlans: Boolean?,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?
)
