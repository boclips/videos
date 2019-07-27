package com.boclips.videos.service.presentation.subject

import com.boclips.videos.service.domain.model.subject.Subject
import org.springframework.hateoas.Resource

class SubjectToResourceConverter {
    fun wrapSubjectIdsInResource(subjects: Set<Subject>): Set<Resource<SubjectResource>> =
        subjects.map { Resource(SubjectResource(id = it.id.value, name = it.name)) }.toSet()

    fun wrapSubjectsInResource(subjects: List<Subject>): List<Resource<SubjectResource>> =
        subjects.map { Resource(SubjectResource.from(it)) }
}
