package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectRepository

class UpdateSubject(private val subjectRepository: SubjectRepository) {
    operator fun invoke(subjectId: SubjectId, name: String?) {
        if (name == null) return

        subjectRepository.updateName(subjectId, name)
    }
}
