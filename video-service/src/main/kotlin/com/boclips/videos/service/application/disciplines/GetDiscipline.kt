package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetDiscipline(
    private val disciplineRepository: DisciplineRepository,
    private val disciplineConverter: DisciplineConverter
) {
    operator fun invoke(disciplineId: String): DisciplineResource {
        val discipline = disciplineRepository.findOne(disciplineId)

        return discipline?.let { disciplineConverter.convert(it) }
            ?: throw ResourceNotFoundApiException("Not found", "Discipline with id $disciplineId cannot be found")
    }
}
