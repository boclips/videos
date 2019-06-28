package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.subjects.SubjectRepository

class DeleteSubject(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(id: SubjectId) {
        subjectRepository.delete(id)
    }
}
