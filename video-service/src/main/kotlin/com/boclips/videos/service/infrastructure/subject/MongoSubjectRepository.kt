package com.boclips.videos.service.infrastructure.subject

import com.boclips.videos.service.domain.model.collection.Subject
import com.boclips.videos.service.domain.model.collection.SubjectId
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.subject.mongo.SubjectDocument
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.getCollection

class MongoSubjectRepository(
    private val mongoClient: MongoClient
) : SubjectRepository {
    companion object : KLogging() {
        const val collectionName = "subjects"
    }

    override fun findAll(): List<Subject> {
        return getSubjectCollection()
            .find()
            .map(this::toSubject)
            .toList()
    }

    override fun create(name: String): Subject {
        val id = ObjectId()
        getSubjectCollection().insertOne(
            SubjectDocument(
                id = id,
                name = name
            )
        )
        return Subject(
            id = SubjectId(
                value = id.toHexString()
            ), name = name
        )
    }

    private fun toSubject(subjectDocument: SubjectDocument): Subject {
        return Subject(
            id = SubjectId(value = subjectDocument.id.toHexString()),
            name = subjectDocument.name
        )
    }

    private fun getSubjectCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<SubjectDocument>(collectionName)
}
