package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetDiscipline(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(disciplineId: String): DisciplineResource {
        return disciplineRepository.findOne(disciplineId)?.let {
            DisciplineConverter.from(it)
        } ?: throw ResourceNotFoundApiException("Not found", "Discipline with id $disciplineId cannot be found")
    }
}
