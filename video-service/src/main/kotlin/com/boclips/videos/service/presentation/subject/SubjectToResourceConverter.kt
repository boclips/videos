package com.boclips.videos.service.presentation.subject

import com.boclips.videos.service.domain.model.Subject
import org.springframework.hateoas.Resource

class SubjectToResourceConverter {
    fun wrapSubjectsInResource(subjects: Set<Subject>): Set<Resource<SubjectResource>> =
        subjects.map { fromSubject(it) }.toSet()

    private fun fromSubject(subject: Subject): Resource<SubjectResource> =
        Resource(SubjectResource(subject.id.value, subject.name))
}
