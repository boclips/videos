package com.boclips.videos.service.infrastructure.subject

import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set

class MongoSubjectRepository(
    private val mongoClient: MongoClient
) : SubjectRepository {
    override fun findByIds(ids: Iterable<String>): List<Subject> {
        val objectIds = ids.map { ObjectId(it) }
        return getSubjectCollection()
            .find(SubjectDocument::id `in` objectIds)
            .map(this::toSubject)
            .toList()
    }

    override fun findByOrderedIds(ids: Iterable<String>): List<Subject> {
        val subjectsById: Map<String, Subject> =
            findByIds(ids).map { it.id.value to it }.toMap()
        return ids.mapNotNull { subjectsById[it] }
    }

    companion object : KLogging() {
        const val collectionName = "subjects"
    }

    override fun findAll(): List<Subject> {
        return getSubjectCollection()
            .find()
            .map(this::toSubject)
            .toList()
    }

    override fun findByName(name: String): Subject? {
        val document = getSubjectCollection()
            .findOne(SubjectDocument::name eq name) ?: return null

        return toSubject(document)
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

    override fun findById(id: SubjectId): Subject? {
        val document = getSubjectCollection()
            .findOne(SubjectDocument::id eq ObjectId(id.value)) ?: return null

        return toSubject(document)
    }

    override fun delete(id: SubjectId) {
        getSubjectCollection().deleteOne(SubjectDocument::id eq ObjectId(id.value))
    }

    override fun update(updateCommand: SubjectUpdateCommand): Subject {
        when (updateCommand) {
            is SubjectUpdateCommand.ReplaceName -> {
                getSubjectCollection().updateOne(
                    SubjectDocument::id eq ObjectId(updateCommand.subjectId.value),
                    set(SubjectDocument::name, updateCommand.name)
                )
            }
        }

        return findById(updateCommand.subjectId)
            ?: throw IllegalStateException("Could not find updated subject ${updateCommand.subjectId.value}")
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
