package com.boclips.videos.service.testsupport.fakes

import com.boclips.security.utils.User
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.EventType
import com.boclips.videos.service.infrastructure.event.RefererHeaderExtractor
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
    fun fakeEventService(): EventService {
        return FakeEventService()
    }
}

class FakeEventService : EventService {
    private val events: MutableList<Event<*>> = mutableListOf()

    fun clear() {
        events.clear()
    }

    private fun <T> saveEvent(type: EventType, data: T) {
        val event = Event(
                type = type,
                timestamp = ZonedDateTime.now(),
                user = UserExtractor.getCurrentUser(),
                data = data,
                url = RefererHeaderExtractor.getReferer()
        )

        events.add(event)
    }

    override fun saveSearchEvent(query: String, pageIndex: Int, pageSize: Int, totalResults: Long) {
        saveEvent(EventType.SEARCH, SearchEventData(query = query, pageIndex = pageIndex, pageSize = pageSize, totalResults = totalResults))
    }

    override fun saveAddToCollectionEvent(collectionId: CollectionId, videoId: AssetId) {
        saveEvent(EventType.ADD_TO_COLLECTION, AddToCollectionEventData(collectionId = collectionId.value, videoId = videoId.value))
    }

    override fun saveRemoveFromCollectionEvent(collectionId: CollectionId, videoId: AssetId) {
        saveEvent(EventType.REMOVE_FROM_COLLECTION, RemoveFromCollectionEventData(collectionId = collectionId.value, videoId = videoId.value))
    }

    override fun savePlaybackEvent(videoId: AssetId, playerId: String, segmentStartSeconds: Long, segmentEndSeconds: Long, videoDurationSeconds: Long) {
        saveEvent(EventType.PLAYBACK, PlaybackEventData(
                playerId = playerId,
                videoId = videoId.value,
                segmentStartSeconds = segmentStartSeconds,
                segmentEndSeconds = segmentEndSeconds,
                videoDurationSeconds = videoDurationSeconds
        ))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> event() = events.single() as Event<T>

    fun searchEvent() = event<SearchEventData>()

    fun playbackEvent() = event<PlaybackEventData>()

    fun addToCollectionEvent() = event<AddToCollectionEventData>()

    fun removeFromCollectionEvent() = event<RemoveFromCollectionEventData>()
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

data class PlaybackEventData(
        val playerId: String,
        val videoId: String,
        val segmentStartSeconds: Long,
        val segmentEndSeconds: Long,
        val videoDurationSeconds: Long
)

data class RemoveFromCollectionEventData(
        val collectionId: String,
        val videoId: String
)
