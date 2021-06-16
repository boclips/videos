package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.api.response.discipline.DisciplinesResource
import com.boclips.videos.api.response.discipline.DisciplinesWrapperResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.domain.model.discipline.Discipline
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder

class DisciplineConverter(private val disciplinesLinkBuilder: DisciplinesLinkBuilder) {
    fun convert(discipline: Discipline): DisciplineResource {
        return DisciplineResource(
            id = discipline.id.value,
            code = discipline.code,
            name = discipline.name,
            subjects = discipline.subjects.map { SubjectResource(id = it.id.value, name = it.name) },
            _links = listOfNotNull(
                disciplinesLinkBuilder.discipline(rel = "self", id = discipline.id.value),
                disciplinesLinkBuilder.subjectsForDiscipline(discipline.id.value),
                disciplinesLinkBuilder.updateDiscipline(discipline.id.value)
            ).map { it.rel.value() to it }.toMap()
        )
    }

    fun convert(disciplines: List<Discipline>): DisciplinesResource {
        return DisciplinesResource(
            _embedded = DisciplinesWrapperResource(disciplines = disciplines.map { convert(it) }),
            _links = listOfNotNull(disciplinesLinkBuilder.disciplines("self")).map { it.rel.value() to it }.toMap()
        )
    }
}
