package com.boclips.videos.service.presentation.disciplines

import com.boclips.videos.service.domain.model.discipline.Discipline
import com.boclips.videos.service.presentation.subject.SubjectResource
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "disciplines")
data class DisciplineResource private constructor(
    val id: String,
    val name: String? = null,
    val code: String? = null,
    val subjects: List<Resource<SubjectResource>>
) {
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
