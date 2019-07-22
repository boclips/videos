package com.boclips.videos.service.application.disciplines

import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.presentation.disciplines.CreateDisciplineRequest
import com.boclips.videos.service.presentation.disciplines.DisciplineResource

class CreateDiscipline(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(request: CreateDisciplineRequest): DisciplineResource {
        return disciplineRepository.create(code = request.code!!, name = request.name!!)
            .let { DisciplineResource.from(it) }
    }
}
