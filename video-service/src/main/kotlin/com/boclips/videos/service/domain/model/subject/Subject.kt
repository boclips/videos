package com.boclips.videos.service.domain.model.subject

import com.boclips.eventbus.domain.Subject as EventSubject

data class Subject(val id: SubjectId, val name: String) {
    fun toEvent(): EventSubject {
        return EventSubject.builder().id(id.toEvent()).name(name).build()
    }
}
