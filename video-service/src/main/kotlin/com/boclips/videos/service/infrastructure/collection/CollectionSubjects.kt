package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectNotFoundException
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.infrastructure.subject.SubjectDocumentConverter

class CollectionSubjects(private val subjectRepository: SubjectRepository) {
    fun getByIds(vararg ids: SubjectId): Set<SubjectDocument> {
        return ids.asSequence()
            .map { subjectRepository.findById(it) ?: throw SubjectNotFoundException(it.value) }
            .map { SubjectDocumentConverter.toSubjectDocument(it) }
            .toSet()
    }
}
