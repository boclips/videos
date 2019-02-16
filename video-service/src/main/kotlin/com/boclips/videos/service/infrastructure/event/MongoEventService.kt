package com.boclips.videos.service.infrastructure.event

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.mongodb.MongoClient
import org.bson.Document
import java.time.ZonedDateTime
import java.util.*

class MongoEventService(
        private val mongoClient: MongoClient
) : EventService {

    override fun saveSearchEvent(query: String, pageIndex: Int, pageSize: Int, totalResults: Long) {
        saveEvent(EventType.SEARCH) {
            append("query", query)
            append("pageIndex", pageIndex)
            append("pageSize", pageSize)
            append("totalResults", totalResults)
        }
    }

    override fun savePlaybackEvent(videoId: AssetId, videoIndex: Int?, playerId: String, segmentStartSeconds: Long, segmentEndSeconds: Long, videoDurationSeconds: Long) {
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
                .append("userId", UserExtractor.getCurrentUser().id)
                .append("userIsBoclips", UserExtractor.getCurrentUser().boclipsEmployee)
                .append("url", RefererHeaderExtractor.getReferer())

        customize(event)

        mongoClient.getDatabase("video-service-db").getCollection("event-log").insertOne(event)
    }

}