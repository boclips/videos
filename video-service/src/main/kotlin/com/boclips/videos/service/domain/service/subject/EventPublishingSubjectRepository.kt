package com.boclips.videos.service.domain.service.subject

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand

class EventPublishingSubjectRepository(val subjectRepository: SubjectRepository, val eventBus: EventBus) :
    SubjectRepository by subjectRepository {

    override fun update(updateCommand: SubjectUpdateCommand): com.boclips.videos.service.domain.model.subject.Subject {
        val subject = subjectRepository.update(updateCommand)

        eventBus.publish(
            SubjectChanged.builder().subject(
                Subject.builder().id(
                    com.boclips.eventbus.domain.SubjectId.builder().value(
                        subject.id.value
                    ).build()
                ).name(subject.name).build()
            ).build()
        )

        return subject
    }
}
