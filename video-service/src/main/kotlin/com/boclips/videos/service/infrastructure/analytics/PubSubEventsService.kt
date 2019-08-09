package com.boclips.videos.service.infrastructure.analytics

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.domain.user.User
import com.boclips.eventbus.events.base.UserEvent
import com.boclips.eventbus.events.collection.CollectionAgeRangeChanged
import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.eventbus.events.collection.CollectionDescriptionChanged
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionSubjectsChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.eventbus.events.video.VideoVisited
import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.common.Do
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.events.EventService

class PubSubEventsService(
    val eventBus: EventBus
) : EventService {
    override fun saveSearchEvent(
        query: String,
        pageIndex: Int,
        pageSize: Int,
        totalResults: Long,
        pageVideoIds: List<String>
    ) {
        eventBus.publish(
            msg(
                VideosSearched.builder()
                    .query(query)
                    .pageIndex(pageIndex)
                    .pageSize(pageSize)
                    .totalResults(totalResults)
                    .pageVideoIds(pageVideoIds)
            )
        )
    }

    override fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommands: List<CollectionUpdateCommand>) {
        updateCommands.forEach { saveUpdateCollectionEvent(collectionId, it) }
    }

    private fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommand: CollectionUpdateCommand) {
        Do exhaustive when (updateCommand) {
            is CollectionUpdateCommand.AddVideoToCollection ->
                eventBus.publish(
                    msg(
                        VideoAddedToCollection.builder()
                            .collectionId(collectionId.value)
                            .videoId(updateCommand.videoId.value)
                    )
                )
            is CollectionUpdateCommand.RemoveVideoFromCollection ->
                eventBus.publish(
                    msg(
                        VideoRemovedFromCollection.builder()
                            .collectionId(collectionId.value)
                            .videoId(updateCommand.videoId.value)
                    )
                )
            is CollectionUpdateCommand.RenameCollection ->
                eventBus.publish(
                    msg(
                        CollectionRenamed.builder()
                            .collectionId(collectionId.value)
                            .collectionTitle(updateCommand.title)
                    )
                )
            is CollectionUpdateCommand.ChangeVisibility ->
                eventBus.publish(
                    msg(
                        CollectionVisibilityChanged.builder()
                            .collectionId(collectionId.value)
                            .isPublic(updateCommand.isPublic)
                    )
                )
            is CollectionUpdateCommand.ReplaceSubjects ->
                eventBus.publish(
                    msg(
                        CollectionSubjectsChanged.builder()
                            .collectionId(collectionId.value)
                            .subjects(updateCommand.subjects.map { it.id.value }.toMutableSet())
                    )
                )
            is CollectionUpdateCommand.ChangeAgeRange ->
                eventBus.publish(
                    msg(
                        CollectionAgeRangeChanged.builder()
                            .collectionId(collectionId.value)
                            .rangeMin(updateCommand.minAge)
                            .rangeMax(updateCommand.maxAge)
                    )
                )
            is CollectionUpdateCommand.RemoveSubjectFromCollection -> eventBus.publish(
                msg(
                    CollectionSubjectsChanged.builder()
                        .collectionId(collectionId.value)
                )
            )

            is CollectionUpdateCommand.ChangeDescription -> eventBus.publish(
                msg(
                    CollectionDescriptionChanged.builder()
                        .collectionId(collectionId.value)
                        .description(updateCommand.description)
                )
            )
        }
    }

    override fun saveBookmarkCollectionEvent(collectionId: CollectionId) {
        eventBus.publish(
            msg(
                CollectionBookmarkChanged.builder().collectionId(collectionId.value).isBookmarked(true)
            )
        )
    }

    override fun saveUnbookmarkCollectionEvent(collectionId: CollectionId) {
        eventBus.publish(
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
        videoDurationSeconds: Long,
        playbackDevice: String?
    ) {
        eventBus.publish(
            msg(
                VideoSegmentPlayed.builder()
                    .videoId(videoId.value)
                    .playerId(playerId)
                    .videoIndex(videoIndex)
                    .segmentStartSeconds(segmentStartSeconds)
                    .segmentEndSeconds(segmentEndSeconds)
                    .videoDurationSeconds(videoDurationSeconds)
                    .playbackDevice(playbackDevice)
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
        eventBus.publish(
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

    override fun saveVideoVisitedEvent(videoId: VideoId) {
        eventBus.publish(msg(VideoVisited.builder().videoId(videoId.value)))
    }

    private fun msg(builder: UserEvent.UserEventBuilder<*, *>): UserEvent {
        val user = getCurrentUser().let {
            User.builder()
                .id(it.id)
                .isBoclipsEmployee(it.boclipsEmployee)
                .build()
        }
        return builder
            .user(user)
            .url(RefererHeaderExtractor.getReferer())
            .build()
    }
}
