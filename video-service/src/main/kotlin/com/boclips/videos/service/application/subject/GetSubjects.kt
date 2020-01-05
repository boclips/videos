package com.boclips.videos.service.application.subject

import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.domain.service.subject.SubjectRepository

class GetSubjects(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(): List<SubjectResource> {
        return subjectRepository.findAll()
            .map { subject ->
                SubjectResource(
                    id = subject.id.value,
                    name = subject.name,
                    lessonPlan = subject.lessonPlan
                )
            }
    }
}
