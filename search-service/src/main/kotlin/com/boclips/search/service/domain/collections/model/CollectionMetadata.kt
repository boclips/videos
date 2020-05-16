package com.boclips.search.service.domain.collections.model

import java.time.LocalDate

data class CollectionMetadata(
    val id: String,
    val title: String,
    val subjectIds: List<String>,
    val curated: Boolean?,
    val owner: String,
    val bookmarkedByUsers: Set<String>,
    val hasAttachments: Boolean,
    val description: String?,
    val hasLessonPlans: Boolean?,
    val promoted: Boolean?,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?,
    val updatedAt: LocalDate,
    val attachmentTypes: Set<String>?
)
