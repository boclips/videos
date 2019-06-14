package com.boclips.videos.service.domain.model.disciplines

import com.boclips.videos.service.domain.model.subjects.Subject

data class Discipline(
    val id: DisciplineId,
    val code: String,
    val name: String,
    val subjects: List<Subject>
)
