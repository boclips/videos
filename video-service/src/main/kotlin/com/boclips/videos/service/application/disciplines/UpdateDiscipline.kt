package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.request.discipline.UpdateDisciplineRequest
import com.boclips.videos.service.domain.service.DisciplineRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class UpdateDiscipline(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(id: String, updateDisciplineRequest: UpdateDisciplineRequest) {
        val discipline = disciplineRepository.findOne(id) ?: throw ResourceNotFoundApiException(
            "Discipline not found",
            "The discipline with id=$id can't be found"
        )

        disciplineRepository.update(
            discipline.copy(
                name = updateDisciplineRequest.name,
                code = updateDisciplineRequest.code
            )
        )
    }
}
