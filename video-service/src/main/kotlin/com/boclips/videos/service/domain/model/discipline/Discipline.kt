package com.boclips.videos.service.domain.model.discipline

import com.boclips.videos.service.domain.model.subject.Subject

data class Discipline(
    val id: DisciplineId,
    val code: String,
    val name: String,
    val subjects: List<Subject>
)
