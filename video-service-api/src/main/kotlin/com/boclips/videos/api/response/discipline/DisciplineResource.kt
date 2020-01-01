package com.boclips.videos.api.response.discipline

import com.boclips.videos.api.response.subject.SubjectResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "disciplines")
data class DisciplineResource constructor(
    val id: String,
    val name: String? = null,
    val code: String? = null,
    val subjects: List<Resource<SubjectResource>>
)