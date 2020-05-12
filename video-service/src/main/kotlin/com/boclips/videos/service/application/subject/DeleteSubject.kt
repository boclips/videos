package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.subject.SubjectService
import mu.KLogging

class DeleteSubject(private val subjectService: SubjectService) {
    companion object : KLogging()

    operator fun invoke(subjectId: SubjectId, user: User) {
        logger.info { "Deleting subject ${subjectId.value} as behalf of ${user.id.value}" }
        subjectService.removeReferences(subjectId = subjectId, user = user)
    }
}
