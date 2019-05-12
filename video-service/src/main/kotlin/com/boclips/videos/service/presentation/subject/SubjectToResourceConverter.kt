package com.boclips.videos.service.presentation.subject

import com.boclips.videos.service.domain.model.collection.SubjectId
import org.springframework.hateoas.Resource

class SubjectToResourceConverter {
    fun wrapSubjectsInResource(subjects: Set<SubjectId>): Set<Resource<SubjectResource>> =
        subjects.map { Resource(SubjectResource(it.value)) }.toSet()
}
