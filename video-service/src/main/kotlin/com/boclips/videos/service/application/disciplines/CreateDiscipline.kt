package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.request.discipline.CreateDisciplineRequest
import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter

class CreateDiscipline(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(request: CreateDisciplineRequest): DisciplineResource {
        return disciplineRepository.create(code = request.code!!, name = request.name!!)
            .let { DisciplineConverter.from(it) }
    }
}
