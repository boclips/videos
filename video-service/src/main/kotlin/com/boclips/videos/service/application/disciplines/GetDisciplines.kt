package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter

class GetDisciplines(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(): List<DisciplineResource> {
        return disciplineRepository.findAll()
            .map { discipline -> DisciplineConverter.from(discipline) }
    }
}
