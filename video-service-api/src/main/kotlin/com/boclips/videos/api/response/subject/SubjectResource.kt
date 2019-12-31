package com.boclips.videos.api.response.subject

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "subjects")
data class SubjectResource(
    val id: String,
    val name: String? = null,
    val lessonPlan: Boolean? = false
)