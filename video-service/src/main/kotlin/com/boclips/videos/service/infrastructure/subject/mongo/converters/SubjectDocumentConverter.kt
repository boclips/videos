package com.boclips.videos.service.infrastructure.subject.mongo.converters

import com.boclips.videos.service.domain.model.collection.Subject
import com.boclips.videos.service.domain.model.collection.SubjectId
import com.boclips.videos.service.infrastructure.subject.mongo.SubjectDocument
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
