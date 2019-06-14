package com.boclips.videos.service.application.disciplines

import com.boclips.videos.service.domain.model.disciplines.DisciplineRepository
import com.boclips.videos.service.presentation.disciplines.DisciplineResource

class GetDisciplines(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(): List<DisciplineResource> {
        return disciplineRepository.findAll()
            .map { discipline -> DisciplineResource.from(discipline) }
    }
}
