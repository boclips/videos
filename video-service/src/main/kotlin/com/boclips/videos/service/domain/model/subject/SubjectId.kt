package com.boclips.videos.service.domain.model.subject

import com.boclips.eventbus.domain.SubjectId as EventSubjectId

data class SubjectId(val value: String) {
    fun toEvent(): EventSubjectId {
        return EventSubjectId(value)
    }
}
