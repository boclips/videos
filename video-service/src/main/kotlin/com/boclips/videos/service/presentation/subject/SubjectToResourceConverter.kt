package com.boclips.videos.service.presentation.subject

import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import org.springframework.hateoas.Resource

class SubjectToResourceConverter {
    fun wrapSubjectIdsInResource(subjects: Set<SubjectId>): Set<Resource<SubjectResource>> =
        subjects.map { Resource(SubjectResource(it.value)) }.toSet()

    fun wrapSubjectsInResource(subjects: List<Subject>): List<Resource<SubjectResource>> =
        subjects.map { Resource(SubjectResource.from(it)) }
}
