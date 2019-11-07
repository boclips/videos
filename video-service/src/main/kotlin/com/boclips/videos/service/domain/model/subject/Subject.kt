package com.boclips.videos.service.domain.model.subject

data class Subject(val id: SubjectId, val name: String, val lessonPlan: Boolean? = false)
