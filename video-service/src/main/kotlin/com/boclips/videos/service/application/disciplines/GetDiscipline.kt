package com.boclips.videos.service.application.disciplines

import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.presentation.disciplines.DisciplineResource
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetDiscipline(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(disciplineId: String): DisciplineResource {
        return disciplineRepository.findOne(disciplineId)?.let {
            DisciplineResource.from(it)
        } ?: throw ResourceNotFoundApiException("Not found", "Discipline with id $disciplineId cannot be found")
    }
}
