package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.request.discipline.CreateDisciplineRequest
import com.boclips.videos.service.domain.model.discipline.Discipline
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository

class CreateDiscipline(private val disciplineRepository: DisciplineRepository) {
    operator fun invoke(request: CreateDisciplineRequest): Discipline {
        return disciplineRepository.create(code = request.code!!, name = request.name!!)
    }
}
