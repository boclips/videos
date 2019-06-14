package com.boclips.videos.service.infrastructure.discipline

import com.boclips.videos.service.domain.model.disciplines.Discipline
import com.boclips.videos.service.domain.model.disciplines.DisciplineId
import com.boclips.videos.service.domain.model.disciplines.DisciplineRepository
import com.boclips.videos.service.domain.model.subjects.SubjectRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection

class MongoDisciplineRepository(
    private val mongoClient: MongoClient,
    private val subjectRepository: SubjectRepository
) : DisciplineRepository {

    override fun findByIds(ids: Iterable<String>): List<Discipline> {
        val objectIds = ids.map { ObjectId(it) }
        return getDisciplineCollection()
                .find(DisciplineDocument::id `in` objectIds)
                .map(this::toDiscipline)
                .toList()
    }

    companion object : KLogging() {
        const val collectionName = "disciplines"
    }

    override fun findOne(id: String) = findByIds(listOf(id)).firstOrNull()

    override fun findAll(): List<Discipline> {
        return getDisciplineCollection()
            .find()
            .map(this::toDiscipline)
            .toList()
    }

    override fun create(code: String, name: String): Discipline {
        val id = ObjectId()
        getDisciplineCollection().insertOne(
            DisciplineDocument(
                id = id,
                name = name,
                code = code,
                subjects = emptyList()
            )
        )
        return Discipline(
            id = DisciplineId(
                value = id.toHexString()
            ), name = name,
            code = code,
            subjects = emptyList()
        )
    }

    override fun update(discipline: Discipline) {
        getDisciplineCollection().replaceOne(DisciplineDocument::id eq ObjectId(discipline.id.value), DisciplineDocumentConverter.toDisciplineDocument(discipline))
    }

    private fun getDisciplineCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<DisciplineDocument>(collectionName)

    private fun toDiscipline(document: DisciplineDocument): Discipline {
        return Discipline(
            id = DisciplineId(document.id.toHexString()),
            name = document.name,
            code = document.code,
            subjects = subjectRepository.findByIds(document.subjects.map { it.toHexString() })
        )
    }
}
