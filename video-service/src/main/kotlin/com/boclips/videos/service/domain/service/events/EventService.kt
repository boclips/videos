package com.boclips.videos.service.domain.service.events

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.base.AbstractEventWithUserId
import com.boclips.eventbus.events.collection.CollectionAgeRangeChanged
import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.eventbus.events.collection.CollectionCreated
import com.boclips.eventbus.events.collection.CollectionDeleted
import com.boclips.eventbus.events.collection.CollectionDescriptionChanged
import com.boclips.eventbus.events.collection.CollectionInteractedWith
import com.boclips.eventbus.events.collection.CollectionInteractionType
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionSubjectsChanged
import com.boclips.eventbus.events.collection.CollectionUpdated
import com.boclips.eventbus.events.collection.CollectionVideosBulkChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.eventbus.events.video.VideoInteractedWith
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.videos.service.common.Do
import com.boclips.videos.service.config.application.UserContext
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.EventConverter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.RefererHeaderExtractor

class EventService(val eventBus: EventBus, private val userContext: UserContext) {
    fun saveSearchEvent(
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

    fun saveUpdateCollectionEvent(updateResult: CollectionUpdateResult) {
        val collection = updateResult.collection
        eventBus.publish(
            CollectionUpdated(
                EventConverter().toCollectionPayload(collection)
            )
        )
        updateResult.commands.forEach { saveUpdateCollectionEvent(it, collection) }
    }

    fun saveCollectionCreatedEvent(collection: Collection) {
        eventBus.publish(
            CollectionCreated(
                EventConverter().toCollectionPayload(collection)
            )
        )
    }

    fun saveCollectionDeletedEvent(collectionId: CollectionId) {
        eventBus.publish(
            msg(
                CollectionDeleted.builder()
                    .collectionId(collectionId.value)
            )
        )
    }

    private fun saveUpdateCollectionEvent(updateCommand: CollectionUpdateCommand, collection: Collection) {
        Do exhaustive when (updateCommand) {
            is CollectionUpdateCommand.AddVideoToCollection ->
                eventBus.publish(
                    msg(
                        VideoAddedToCollection.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .videoId(updateCommand.videoId.value)
                    )
                )
            is CollectionUpdateCommand.RemoveVideoFromCollection ->
                eventBus.publish(
                    msg(
                        VideoRemovedFromCollection.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .videoId(updateCommand.videoId.value)
                    )
                )
            is CollectionUpdateCommand.RenameCollection ->
                eventBus.publish(
                    msg(
                        CollectionRenamed.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .collectionTitle(updateCommand.title)
                    )
                )
            is CollectionUpdateCommand.ChangeVisibility ->
                eventBus.publish(
                    msg(
                        CollectionVisibilityChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .isPublic(updateCommand.isPublic)
                    )
                )
            is CollectionUpdateCommand.ReplaceSubjects, is CollectionUpdateCommand.RemoveSubjectFromCollection ->
                eventBus.publish(
                    msg(
                        CollectionSubjectsChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .subjects(collection.subjects.map { it.id.value }.toMutableSet())
                    )
                )
            is CollectionUpdateCommand.ChangeAgeRange ->
                eventBus.publish(
                    msg(
                        CollectionAgeRangeChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .rangeMin(updateCommand.minAge)
                            .rangeMax(updateCommand.maxAge)
                    )
                )

            is CollectionUpdateCommand.ChangeDescription -> eventBus.publish(
                msg(
                    CollectionDescriptionChanged.builder()
                        .collectionId(updateCommand.collectionId.value)
                        .description(updateCommand.description)
                )
            )

            is CollectionUpdateCommand.AddAttachment -> null// no event as this cannot be done in the app just yet
            is CollectionUpdateCommand.BulkUpdateCollectionVideos -> eventBus.publish(
                msg(
                    CollectionVideosBulkChanged.builder()
                        .collectionId(updateCommand.collectionId.value)
                        .videoIds(updateCommand.videoIds.map { it.value })
                )
            )
            is CollectionUpdateCommand.Bookmark -> eventBus.publish(
                msg(
                    CollectionBookmarkChanged.builder().collectionId(updateCommand.collectionId.value).isBookmarked(true)
                )
            )
            is CollectionUpdateCommand.Unbookmark -> eventBus.publish(
                msg(
                    CollectionBookmarkChanged.builder().collectionId(updateCommand.collectionId.value).isBookmarked(
                        false
                    )
                )
            )
        }
    }

    fun publishVideoInteractedWithEvent(videoId: VideoId, subtype: String) {
        eventBus.publish(
            msg(
                VideoInteractedWith.builder().videoId(videoId.value).subtype(subtype)
            )
        )
    }

    fun savePlaybackEvent(
        videoId: VideoId,
        videoIndex: Int?,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        playbackDevice: String?
    ) {
        eventBus.publish(
            msg(
                VideoSegmentPlayed.builder()
                    .videoId(videoId.value)
                    .videoIndex(videoIndex)
                    .segmentStartSeconds(segmentStartSeconds)
                    .segmentEndSeconds(segmentEndSeconds)
                    .playbackDevice(playbackDevice)
            )
        )
    }

    fun savePlayerInteractedWithEvent(
        videoId: VideoId,
        currentTime: Long,
        subtype: String,
        payload: Map<String, Any>
    ) {
        eventBus.publish(
            msg(
                VideoPlayerInteractedWith.builder()
                    .videoId(videoId.value)
                    .currentTime(currentTime)
                    .subtype(subtype)
                    .payload(payload)
            )
        )
    }

    fun saveCollectionInteractedWithEvent(collectionId: String, subtype: CollectionInteractionType) {
        eventBus.publish(
            msg(
                CollectionInteractedWith.builder()
                    .collectionId(collectionId)
                    .subtype(subtype)
            )
        )
    }

    private fun msg(builder: AbstractEventWithUserId.AbstractEventWithUserIdBuilder<*, *>): AbstractEventWithUserId {
        return builder
            .userId(userContext.getUser().value)
            .url(RefererHeaderExtractor.getReferer())
            .build()
    }
}
