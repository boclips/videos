package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.subject.SubjectResource

class GetSubjects(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(): List<SubjectResource> {
        return subjectRepository.findAll()
            .map { subject -> SubjectResource(id = subject.id.value, name = subject.name, lessonPlan = subject.lessonPlan) }
    }
}
