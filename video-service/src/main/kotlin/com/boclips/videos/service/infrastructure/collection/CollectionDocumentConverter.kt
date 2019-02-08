package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.infrastructure.video.mongo.getList
import org.bson.Document
import org.bson.types.ObjectId

class CollectionDocumentConverter {
    fun toDocument(collectionDocument: CollectionDocument): Document {
        return Document()
            .append("_id", ObjectId(collectionDocument.id))
            .append("title", collectionDocument.title)
            .append("owner", collectionDocument.owner)
            .append("videos", collectionDocument.videos)
    }

    fun fromDocument(document: Document): CollectionDocument {
        return CollectionDocument(
            id = document.getObjectId("_id").toHexString(),
            owner = document.getString("owner"),
            title = document.getString("title"),
            videos = document.getList("videos")
        )
    }
}