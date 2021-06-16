package com.boclips.videos.service.infrastructure.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.service.TagRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.TagDocument
import com.boclips.videos.service.infrastructure.video.UserTagDocument
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
            .find(UserTagDocument::id `in` objectIds)
            .map(::toTag)
            .toList()
    }

    companion object : KLogging() {
        const val collectionName = "tags"
    }

    override fun findAll(): List<Tag> {
        return getTagCollection()
            .find()
            .map(::toTag)
            .toList()
    }

    override fun findByLabel(label: String): Tag? {
        val document = getTagCollection()
            .findOne(UserTagDocument::label eq label) ?: return null

        return toTag(document)
    }

    override fun create(label: String): Tag {
        val id = ObjectId()
        getTagCollection().insertOne(
            TagDocument(
                id = id,
                label = label
            )
        )
        return Tag(
            id = TagId(
                value = id.toHexString()
            ),
            label = label
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
            label = tagDocument.label
        )
    }

    private fun getTagCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<TagDocument>(collectionName)
}
