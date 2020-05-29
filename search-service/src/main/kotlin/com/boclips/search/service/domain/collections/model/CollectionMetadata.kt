package com.boclips.search.service.domain.collections.model

import java.time.LocalDate

data class CollectionMetadata(
    val id: String,
    val title: String,
    val owner: String,
    val description: String?,
    val discoverable: Boolean?,
    val hasAttachments: Boolean,
    val hasLessonPlans: Boolean?,
    val promoted: Boolean?,
    val default: Boolean,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?,
    val updatedAt: LocalDate,
    val subjectIds: List<String>,
    val bookmarkedByUsers: Set<String>,
    val attachmentTypes: Set<String>?
)
