package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.domain.model.discipline.Discipline

class DisciplineConverter {
    companion object {
        fun from(discipline: Discipline) =
            DisciplineResource(
                id = discipline.id.value,
                code = discipline.code,
                name = discipline.name,
                subjects = SubjectToResourceConverter().wrapSubjectsInResource(discipline.subjects)
            )
    }
}