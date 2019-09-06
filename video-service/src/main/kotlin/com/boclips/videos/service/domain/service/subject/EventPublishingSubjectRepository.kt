package com.boclips.videos.service.domain.service.subject

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.model.subject.SubjectId

class EventPublishingSubjectRepository(val subjectRepository: SubjectRepository, val eventBus: EventBus) :
    SubjectRepository by subjectRepository {

    override fun updateName(subjectId: SubjectId, name: String) {
        subjectRepository.updateName(subjectId, name)

        val subject =
            subjectRepository.findById(subjectId) ?: throw IllegalStateException("Could not find updated subject")

        eventBus.publish(
            SubjectChanged.builder().subject(
                Subject.builder().id(
                    com.boclips.eventbus.domain.SubjectId.builder().value(
                        subject.id.value
                    ).build()
                ).name(subject.name).build()
            ).build()
        )
    }
}
