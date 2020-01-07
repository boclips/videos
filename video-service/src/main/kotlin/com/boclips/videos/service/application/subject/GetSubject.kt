package com.boclips.videos.service.application.subject

import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetSubject(private val subjectRepository: SubjectRepository) {
    operator fun invoke(subjectId: String): SubjectResource {
        return subjectRepository.findByIds(listOf(subjectId)).firstOrNull()?.let {
            SubjectResource(
                id = it.id.value,
                name = it.name
            )
        } ?: throw ResourceNotFoundApiException("Not found", "Subject with id $subjectId cannot be found")
    }
}
