package com.boclips.videos.service.infrastructure.analytics

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import getCurrentUser
import org.bson.Document
import java.time.ZonedDateTime
import java.util.Date

class MongoAnalyticsEventService(
    private val mongoClient: MongoClient
) : AnalyticsEventService {
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

    override fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommands: List<CollectionUpdateCommand>) {
        updateCommands.forEach { updateCommand ->
            when (updateCommand) {
                is CollectionUpdateCommand.RenameCollectionCommand -> saveEvent(EventType.RENAME_COLLECTION) {
                    append("title", updateCommand.title)
                    append("collectionId", collectionId.value)
                }
                is CollectionUpdateCommand.ChangeVisibilityCommand -> saveEvent(EventType.CHANGE_VISIBILITY) {
                    append("collectionId", collectionId.value)
                    append("isPublic", updateCommand.isPublic)
                }
                is CollectionUpdateCommand.AddVideoToCollectionCommand -> saveEvent(EventType.ADD_TO_COLLECTION) {
                    append("assetId", updateCommand.videoId.value)
                    append("collectionId", collectionId.value)
                }
                is CollectionUpdateCommand.RemoveVideoFromCollectionCommand -> saveEvent(EventType.REMOVE_FROM_COLLECTION) {
                    append("assetId", updateCommand.videoId.value)
                    append("collectionId", collectionId.value)
                }
            }
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