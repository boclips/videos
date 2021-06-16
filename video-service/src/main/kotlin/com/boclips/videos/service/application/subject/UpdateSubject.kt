package com.boclips.videos.service.application.subject

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.subject.SubjectService
import mu.KLogging

class UpdateSubject(private val subjectService: SubjectService) {
    companion object : KLogging()

    operator fun invoke(subjectId: SubjectId, name: String?) {
        if (name == null) return
        subjectService.renameOnly(subjectId = subjectId, name = name)
    }

    @BoclipsEventListener
    fun onUpdatedSubject(subjectChanged: SubjectChanged) {
        val updatedSubject = Subject(SubjectId(subjectChanged.subject.id.value), subjectChanged.subject.name)
        subjectService.replaceReferences(updatedSubject)
    }
}
