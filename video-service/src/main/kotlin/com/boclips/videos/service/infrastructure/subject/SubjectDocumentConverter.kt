package com.boclips.videos.service.infrastructure.subject

import com.boclips.videos.service.domain.model.subjects.Subject
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import org.bson.types.ObjectId

object SubjectDocumentConverter {

    fun toSubject(document: SubjectDocument): Subject {
        return Subject(
            id = SubjectId(document.id.toHexString()),
            name = document.name
        )
    }

    fun toSubjectDocument(subject: Subject): SubjectDocument {
        return SubjectDocument(
            id = ObjectId(subject.id.value),
            name = subject.name
        )
    }

}
