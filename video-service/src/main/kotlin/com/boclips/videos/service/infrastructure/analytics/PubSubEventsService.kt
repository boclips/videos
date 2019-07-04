package com.boclips.videos.service.infrastructure.analytics

import com.boclips.events.config.Topics
import com.boclips.events.types.User
import com.boclips.events.types.base.Event
import com.boclips.events.types.base.UserEvent
import com.boclips.events.types.collection.CollectionAgeRangeChanged
import com.boclips.events.types.collection.CollectionBookmarkChanged
import com.boclips.events.types.collection.CollectionRenamed
import com.boclips.events.types.collection.CollectionSubjectsChanged
import com.boclips.events.types.collection.CollectionVisibilityChanged
import com.boclips.events.types.collection.VideoAddedToCollection
import com.boclips.events.types.collection.VideoRemovedFromCollection
import com.boclips.events.types.video.VideoPlayerInteractedWith
import com.boclips.events.types.video.VideoSegmentPlayed
import com.boclips.events.types.video.VideosSearched
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.common.Do
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.events.EventService
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

class PubSubEventsService(
    val topics: Topics
) : EventService {
    override fun saveSearchEvent(query: String, pageIndex: Int, pageSize: Int, totalResults: Long) {
        topics.videosSearched().send(
            msg(
                VideosSearched.builder()
                    .query(query)
                    .pageIndex(pageIndex)
                    .pageSize(pageSize)
                    .totalResults(totalResults)
            )
        )
    }

    override fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommands: List<CollectionUpdateCommand>) {
        updateCommands.forEach { saveUpdateCollectionEvent(collectionId, it) }
    }

    private fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommand: CollectionUpdateCommand) {
        Do exhaustive when (updateCommand) {
            is CollectionUpdateCommand.AddVideoToCollection ->
                topics.videoAddedToCollection().send(
                    msg(
                        VideoAddedToCollection.builder()
                            .collectionId(collectionId.value)
                            .videoId(updateCommand.videoId.value)
                    )
                )
            is CollectionUpdateCommand.RemoveVideoFromCollection ->
                topics.videoRemovedFromCollection().send(
                    msg(
                        VideoRemovedFromCollection.builder()
                            .collectionId(collectionId.value)
                            .videoId(updateCommand.videoId.value)
                    )
                )
            is CollectionUpdateCommand.RenameCollection ->
                topics.collectionRenamed().send(
                    msg(
                        CollectionRenamed.builder()
                            .collectionId(collectionId.value)
                            .collectionTitle(updateCommand.title)
                    )
                )
            is CollectionUpdateCommand.ChangeVisibility ->
                topics.collectionVisibilityChanged().send(
                    msg(
                        CollectionVisibilityChanged.builder()
                            .collectionId(collectionId.value)
                            .isPublic(updateCommand.isPublic)
                    )
                )
            is CollectionUpdateCommand.ReplaceSubjects ->
                topics.collectionSubjectsChanged().send(
                    msg(
                        CollectionSubjectsChanged.builder()
                            .collectionId(collectionId.value)
                            .subjects(updateCommand.subjects.map { it.value }.toMutableSet())
                    )
                )
            is CollectionUpdateCommand.ChangeAgeRange ->
                topics.collectionAgeRangeChanged().send(
                    msg(
                        CollectionAgeRangeChanged.builder()
                            .collectionId(collectionId.value)
                            .rangeMin(updateCommand.minAge)
                            .rangeMax(updateCommand.maxAge)
                    )
                )
        }
    }

    override fun saveBookmarkCollectionEvent(collectionId: CollectionId) {
        topics.collectionBookmarkChanged().send(
            msg(
                CollectionBookmarkChanged.builder().collectionId(collectionId.value).isBookmarked(true)
            )
        )
    }

    override fun saveUnbookmarkCollectionEvent(collectionId: CollectionId) {
        topics.collectionBookmarkChanged().send(
            msg(
                CollectionBookmarkChanged.builder().collectionId(collectionId.value).isBookmarked(false)
            )
        )
    }

    override fun savePlaybackEvent(
        videoId: VideoId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long
    ) {
        topics.videoSegmentPlayed().send(
            msg(
                VideoSegmentPlayed.builder()
                    .videoId(videoId.value)
                    .playerId(playerId)
                    .videoIndex(videoIndex)
                    .segmentStartSeconds(segmentStartSeconds)
                    .segmentEndSeconds(segmentEndSeconds)
                    .videoDurationSeconds(videoDurationSeconds)
            )
        )
    }

    override fun savePlayerInteractedWithEvent(
        playerId: String,
        videoId: VideoId,
        videoDurationSeconds: Long,
        currentTime: Long,
        subtype: String,
        payload: Map<String, Any>
    ) {
        topics.videoPlayerInteractedWith().send(
            msg(
                VideoPlayerInteractedWith.builder()
                    .playerId(playerId)
                    .videoId(videoId.value)
                    .videoDurationSeconds(videoDurationSeconds)
                    .currentTime(currentTime)
                    .subtype(subtype)
                    .payload(payload)
            )
        )
    }

    private fun msg(builder: UserEvent.UserEventBuilder<*, *>): Message<*> {
        val user = getCurrentUser().let {
            User.builder()
                .id(it.id)
                .isBoclipsEmployee(it.boclipsEmployee)
                .build()
        }
        return MessageBuilder.withPayload<Event?>(
            builder
                .user(user)
                .url(RefererHeaderExtractor.getReferer())
                .build()
        ).build()
    }
}
