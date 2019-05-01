package com.boclips.videos.service.testsupport.fakes

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import com.boclips.videos.service.infrastructure.analytics.EventType
import com.boclips.videos.service.infrastructure.analytics.RefererHeaderExtractor
import getCurrentUser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.ZonedDateTime

@Profile("fake-event-service")
@Configuration
class EventServiceFakeConfiguration {
    @Bean
    @Primary
    fun fakeEventService(): AnalyticsEventService {
        return FakeAnalyticsEventService()
    }
}

class FakeAnalyticsEventService : AnalyticsEventService {
    private val events: MutableList<Event<*>> = mutableListOf()

    fun clear() {
        events.clear()
    }

    private fun <T> saveEvent(type: EventType, data: T) {
        val event = Event(
            type = type,
            timestamp = ZonedDateTime.now(),
            user = getCurrentUser(),
            data = data,
            url = RefererHeaderExtractor.getReferer()
        )

        events.add(event)
    }

    override fun saveSearchEvent(query: String, pageIndex: Int, pageSize: Int, totalResults: Long) {
        saveEvent(
            EventType.SEARCH,
            SearchEventData(query = query, pageIndex = pageIndex, pageSize = pageSize, totalResults = totalResults)
        )
    }

    override fun saveBookmarkCollectionEvent(collectionId: CollectionId) {
        saveEvent(EventType.BOOKMARK, BookmarkEventData(collectionId.value))
    }

    override fun saveUnbookmarkCollectionEvent(collectionId: CollectionId) {
        saveEvent(EventType.UNBOOKMARK, UnbookmarkEventData(collectionId.value))
    }

    override fun savePlaybackEvent(
        videoId: AssetId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long
    ) {
        saveEvent(
            EventType.PLAYBACK, PlaybackEventData(
                playerId = playerId,
                videoId = videoId.value,
                videoIndex = videoIndex,
                segmentStartSeconds = segmentStartSeconds,
                segmentEndSeconds = segmentEndSeconds,
                videoDurationSeconds = videoDurationSeconds
            )
        )
    }

    override fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommands: List<CollectionUpdateCommand>) {
        updateCommands.forEach { saveEvent(collectionId, it) }
    }

    private fun saveEvent(collectionId: CollectionId, updateCommand: CollectionUpdateCommand) {
        return when (updateCommand) {
            is CollectionUpdateCommand.RenameCollectionCommand -> saveEvent(
                EventType.RENAME_COLLECTION, RenameCollectionEvent(
                    collectionId = collectionId.value,
                    collectionTitle = updateCommand.title
                )
            )
            is CollectionUpdateCommand.ChangeVisibilityCommand -> saveEvent(
                EventType.CHANGE_VISIBILITY, ChangeVisibilityOfCollectionEvent(
                    collectionId = collectionId.value,
                    isPublic = updateCommand.isPublic
                )
            )
            is CollectionUpdateCommand.AddVideoToCollectionCommand -> saveEvent(
                EventType.ADD_TO_COLLECTION,
                AddToCollectionEventData(collectionId = collectionId.value, videoId = updateCommand.videoId.value)
            )
            is CollectionUpdateCommand.RemoveVideoFromCollectionCommand -> saveEvent(
                EventType.REMOVE_FROM_COLLECTION,
                RemoveFromCollectionEventData(collectionId = collectionId.value, videoId = updateCommand.videoId.value)
            )
            is CollectionUpdateCommand.ReplaceSubjectsCommand -> saveEvent(
                EventType.UPDATE_SUBJECTS,
                UpdatedCollectionSubjectsEventData(
                    collectionId = collectionId.value,
                    updatedSubjects = updateCommand.subjects.map { it.value })
            )
            is CollectionUpdateCommand.ChangeAgeRangeCommand -> saveEvent(
                EventType.UPDATE_AGE_RANGE,
                UpdatedAgeRangeEventData(
                    collectionId = collectionId.value,
                    minAge = updateCommand.minAge,
                    maxAge = updateCommand.maxAge
                )
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> event() = events.single() as Event<T>

    fun searchEvent() = event<SearchEventData>()

    fun bookmarkEvent() = event<BookmarkEventData>()

    fun unbookmarkEvent() = event<UnbookmarkEventData>()

    fun playbackEvent() = event<PlaybackEventData>()

    fun addToCollectionEvent() = event<AddToCollectionEventData>()

    fun removeFromCollectionEvent() = event<RemoveFromCollectionEventData>()

    fun renameCollectionEvent() = event<RenameCollectionEvent>()

    fun changeVisibilityOfCollectionEvent() = event<ChangeVisibilityOfCollectionEvent>()
}

class Event<TData>(
    val type: EventType,
    val timestamp: ZonedDateTime,
    val user: User,
    val data: TData,
    val url: String?
)

data class SearchEventData(
    val query: String,
    val pageIndex: Int,
    val pageSize: Int,
    val totalResults: Long
)

data class AddToCollectionEventData(
    val collectionId: String,
    val videoId: String
)

data class BookmarkEventData(
    val collectionId: String
)

data class UnbookmarkEventData(
    val collectionId: String
)

data class PlaybackEventData(
    val playerId: String,
    val videoId: String,
    val videoIndex: Int?,
    val segmentStartSeconds: Long,
    val segmentEndSeconds: Long,
    val videoDurationSeconds: Long
)

data class RemoveFromCollectionEventData(
    val collectionId: String,
    val videoId: String
)

data class RenameCollectionEvent(
    val collectionId: String,
    val collectionTitle: String
)

data class ChangeVisibilityOfCollectionEvent(
    val collectionId: String,
    val isPublic: Boolean
)

data class UpdatedCollectionSubjectsEventData(
    val collectionId: String,
    val updatedSubjects: List<String>
)

data class UpdatedAgeRangeEventData(
    val collectionId: String,
    val minAge: Int,
    val maxAge: Int
)