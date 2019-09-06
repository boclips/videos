package com.boclips.videos.service.domain.model.subject

sealed class SubjectUpdateCommand(val subjectId: SubjectId) {
    class ReplaceName(subjectId: SubjectId, val name: String) : SubjectUpdateCommand(subjectId)
}
