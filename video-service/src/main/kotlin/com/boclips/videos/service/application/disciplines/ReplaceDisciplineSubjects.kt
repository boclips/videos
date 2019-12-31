package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter
import com.boclips.web.exceptions.ResourceNotFoundApiException

class ReplaceDisciplineSubjects(
    private val disciplineRepository: DisciplineRepository,
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(disciplineId: String, subjectUris: List<String>): DisciplineResource {
        val discipline = disciplineRepository.findOne(disciplineId) ?: throw ResourceNotFoundApiException(
            "Discipline not found",
            "The discipline with id=$disciplineId can't be found"
        )

        return discipline.copy(subjects = subjectRepository.findByIds(subjectUris.map { it.substringAfter("/subjects/") }))
            .let {
                disciplineRepository.update(discipline = it)
                DisciplineConverter.from(it)
            }
    }
}
