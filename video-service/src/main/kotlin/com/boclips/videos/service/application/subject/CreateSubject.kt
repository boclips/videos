package com.boclips.videos.service.application.subject

import com.boclips.videos.service.application.exceptions.SubjectExistsException
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest

class CreateSubject(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(request: CreateSubjectRequest): Subject {
        val subjectName = request.name!!

        if (subjectRepository.findByName(subjectName) != null) {
            throw SubjectExistsException(subjectName)
        }
        return subjectRepository.create(name = subjectName)
    }
}
