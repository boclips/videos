package com.boclips.videos.service.infrastructure.contentwarning

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.domain.service.ContentWarningRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.ContentWarningDocument
import com.boclips.videos.service.infrastructure.video.converters.ContentWarningDocumentConverter
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoContentWarningRepository(
    private val mongoClient: MongoClient
) : ContentWarningRepository {
    companion object : KLogging() {
        const val collectionName = "contentWarnings"
    }

    override fun findById(id: ContentWarningId): ContentWarning? {
        return getCollection().findOne(ContentWarningDocument::id eq ObjectId(id.value))
            ?.let { ContentWarningDocumentConverter.toContentWarning(it) }
    }

    override fun findAll(): List<ContentWarning> =
        getCollection().find().map { ContentWarningDocumentConverter.toContentWarning(it) }.toList()

    override fun create(label: String): ContentWarning {
        val id = ObjectId()
        getCollection().insertOne(
            ContentWarningDocument(
                id = id,
                label = label
            )
        )

        return ContentWarning(id = ContentWarningId(value = id.toHexString()), label = label)
    }

    private fun getCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentWarningDocument>(collectionName)
}
