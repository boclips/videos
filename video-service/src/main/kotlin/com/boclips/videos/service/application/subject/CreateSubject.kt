package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.subjects.SubjectRepository
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.subject.SubjectResource

class CreateSubject(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(request: CreateSubjectRequest): SubjectResource {
        return subjectRepository.create(name = request.name!!)
            .let { SubjectResource(id = it.id.value, name = it.name) }
    }
}
