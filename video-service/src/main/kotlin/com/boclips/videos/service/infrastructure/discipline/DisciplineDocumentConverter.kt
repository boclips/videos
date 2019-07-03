package com.boclips.videos.service.infrastructure.discipline

import com.boclips.videos.service.domain.model.disciplines.Discipline
import org.bson.types.ObjectId

object DisciplineDocumentConverter {

    fun toDisciplineDocument(discipline: Discipline): DisciplineDocument {
        return DisciplineDocument(
            id = ObjectId(discipline.id.value),
            name = discipline.name,
            code = discipline.code,
            subjects = discipline.subjects.map { ObjectId(it.id.value) }
        )
    }
}
