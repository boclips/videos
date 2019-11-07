package com.boclips.videos.service.presentation.subject

import com.boclips.videos.service.domain.model.subject.Subject
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "subjects")
data class SubjectResource(
    val id: String,
    val name: String? = null,
    val lessonPlan: Boolean? = false
) {
    companion object {
        fun from(subject: Subject) = SubjectResource(
            id = subject.id.value,
            name = subject.name,
            lessonPlan = subject.lessonPlan
        )
    }
}
