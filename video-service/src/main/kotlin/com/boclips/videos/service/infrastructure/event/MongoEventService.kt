package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import getCurrentUser
import org.bson.Document
import java.time.ZonedDateTime
import java.util.Date

class MongoEventService(
    private val mongoClient: MongoClient
) : EventService {
    companion object {
        const val collectionName = "event-log"
    }

    override fun saveSearchEvent(query: String, pageIndex: Int, pageSize: Int, totalResults: Long) {
        saveEvent(EventType.SEARCH) {
            append("query", query)
            append("pageIndex", pageIndex)
            append("pageSize", pageSize)
            append("totalResults", totalResults)
        }
    }

    override fun savePlaybackEvent(
        videoId: AssetId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long
    ) {
        saveEvent(EventType.PLAYBACK) {
            append("assetId", videoId.value)
            append("videoIndex", videoIndex)
            append("playerId", playerId)
            append("segmentStartSeconds", segmentStartSeconds)
            append("segmentEndSeconds", segmentEndSeconds)
            append("videoDurationSeconds", videoDurationSeconds)
        }
    }

    override fun saveAddToCollectionEvent(collectionId: CollectionId, videoId: AssetId) {
        saveEvent(EventType.ADD_TO_COLLECTION) {
            append("assetId", videoId.value)
            append("collectionId", collectionId.value)
        }
    }

    override fun saveRemoveFromCollectionEvent(collectionId: CollectionId, videoId: AssetId) {
        saveEvent(EventType.REMOVE_FROM_COLLECTION) {
            append("assetId", videoId.value)
            append("collectionId", collectionId.value)
        }
    }

    private fun saveEvent(type: EventType, customize: Document.() -> Unit) {
        val event = Document()
            .append("type", type.name)
            .append("timestamp", Date.from(ZonedDateTime.now().toInstant()))
            .append("userId", getCurrentUser().id)
            .append("userIsBoclips", getCurrentUser().boclipsEmployee)
            .append("url", RefererHeaderExtractor.getReferer())

        customize(event)

        mongoClient.getDatabase(DATABASE_NAME).getCollection(collectionName).insertOne(event)
    }
}