package com.boclips.videos.service.application.subject

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.application.exceptions.SubjectExistsException
import com.boclips.videos.service.domain.model.subjects.SubjectRepository
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.subject.SubjectResource

class CreateSubject(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(request: CreateSubjectRequest): SubjectResource {
        val subjectName = getOrThrow(request.name, "name")

        if (subjectRepository.findByName(subjectName) != null) {
            throw SubjectExistsException(subjectName)
        }
        return subjectRepository.create(name = subjectName)
            .let { SubjectResource(id = it.id.value, name = it.name) }
    }
}
