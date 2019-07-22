package com.boclips.videos.service.infrastructure.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoTagRepository(
    private val mongoClient: MongoClient
) : TagRepository {
    override fun findByIds(ids: Iterable<String>): List<Tag> {
        val objectIds = ids.map { ObjectId(it) }
        return getTagCollection()
            .find(TagDocument::id `in` objectIds)
            .map(this::toTag)
            .toList()
    }

    companion object : KLogging() {
        const val collectionName = "tags"
    }

    override fun findAll(): List<Tag> {
        return getTagCollection()
            .find()
            .map(this::toTag)
            .toList()
    }

    override fun findByName(name: String): Tag? {
        val document = getTagCollection()
            .findOne(TagDocument::name eq name) ?: return null

        return toTag(document)
    }

    override fun create(name: String): Tag {
        val id = ObjectId()
        getTagCollection().insertOne(
            TagDocument(
                id = id,
                name = name
            )
        )
        return Tag(
            id = TagId(
                value = id.toHexString()
            ), name = name
        )
    }

    override fun findById(id: TagId): Tag? {
        val document = getTagCollection()
            .findOne(TagDocument::id eq ObjectId(id.value)) ?: return null

        return toTag(document)
    }

    override fun delete(id: TagId) {
        getTagCollection().deleteOne(TagDocument::id eq ObjectId(id.value))
    }

    private fun toTag(tagDocument: TagDocument): Tag {
        return Tag(
            id = TagId(value = tagDocument.id.toHexString()),
            name = tagDocument.name
        )
    }

    private fun getTagCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<TagDocument>(collectionName)
}
