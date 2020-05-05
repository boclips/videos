package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.response.discipline.DisciplinesResource
import com.boclips.videos.service.domain.service.DisciplineRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter

class GetDisciplines(
    private val disciplineRepository: DisciplineRepository,
    private val disciplineConverter: DisciplineConverter
) {
    operator fun invoke(): DisciplinesResource {
        val disciplines = disciplineRepository.findAll()

        return disciplineConverter.convert(disciplines = disciplines)
    }
}
