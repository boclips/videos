package com.boclips.videos.service.application.disciplines

import com.boclips.videos.service.domain.model.discipline.Discipline
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.service.DisciplineRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class ReplaceDisciplineSubjects(
    private val disciplineRepository: DisciplineRepository,
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(disciplineId: String, subjectIds: List<String>): Discipline {
        val discipline = disciplineRepository.findOne(disciplineId) ?: throw ResourceNotFoundApiException(
            "Discipline not found",
            "The discipline with id=$disciplineId can't be found"
        )

        val subjectsToUpdate: List<Subject> = subjectRepository.findByOrderedIds(subjectIds)
        val updatedDiscipline = discipline.copy(subjects = subjectsToUpdate)
        updatedDiscipline.let { disciplineRepository.update(discipline = it) }

        return updatedDiscipline
    }
}
