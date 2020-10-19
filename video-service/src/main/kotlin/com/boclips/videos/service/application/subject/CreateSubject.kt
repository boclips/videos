package com.boclips.videos.service.application.subject

import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.service.application.exceptions.SubjectExistsException
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex
import mu.KLogging

class CreateSubject(private val subjectRepository: SubjectRepository, private val subjectIndex: SubjectIndex) {
    companion object : KLogging()

    operator fun invoke(request: CreateSubjectRequest): Subject {
        val subjectName = request.name!!

        if (subjectRepository.findByName(subjectName) != null) {
            throw SubjectExistsException(subjectName)
        }

        val createdSubject = subjectRepository.create(name = subjectName)

        subjectIndex.upsert(
            sequenceOf(
                SubjectSuggestion(
                    name = createdSubject.name,
                    id = createdSubject.id
                )
            )
        )

        logger.info { "Created subject $createdSubject" }

        return createdSubject
    }
}
