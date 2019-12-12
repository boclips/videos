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
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.EventConverter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.RefererHeaderExtractor
import java.time.ZonedDateTime

class EventService(val eventBus: EventBus) {
    fun saveSearchEvent(
        query: String,
        pageIndex: Int,
        pageSize: Int,
        totalResults: Long,
        pageVideoIds: List<String>,
        user: User
    ) {
        eventBus.publish(
            msg(
                builder = VideosSearched.builder()
                    .query(query)
                    .pageIndex(pageIndex)
                    .pageSize(pageSize)
                    .totalResults(totalResults)
                    .pageVideoIds(pageVideoIds),
                userId = user.id
            )
        )
    }

    fun saveCollectionCreatedEvent(collection: Collection) {
        eventBus.publish(CollectionCreated(EventConverter().toCollectionPayload(collection)))
    }

    fun saveCollectionDeletedEvent(collectionId: CollectionId, user: User) {
        eventBus.publish(
            msg(
                builder = CollectionDeleted.builder()
                    .collectionId(collectionId.value),
                userId = user.id
            )
        )
    }

    fun saveUpdateCollectionEvent(updateResult: CollectionUpdateResult) {
        val collection = updateResult.collection

        eventBus.publish(CollectionUpdated(EventConverter().toCollectionPayload(collection)))

        updateResult.commands.forEach { saveUpdateCollectionEvent(it, collection) }
    }

    private fun saveUpdateCollectionEvent(updateCommand: CollectionUpdateCommand, collection: Collection) {
        Do exhaustive when (updateCommand) {
            is CollectionUpdateCommand.AddVideoToCollection ->
                eventBus.publish(
                    msg(
                        builder = VideoAddedToCollection.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .videoId(updateCommand.videoId.value),
                        userId = updateCommand.user.id
                    )
                )
            is CollectionUpdateCommand.RemoveVideoFromCollection ->
                eventBus.publish(
                    msg(
                        builder = VideoRemovedFromCollection.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .videoId(updateCommand.videoId.value),
                        userId = updateCommand.user.id
                    )
                )
            is CollectionUpdateCommand.RenameCollection ->
                eventBus.publish(
                    msg(
                        builder = CollectionRenamed.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .collectionTitle(updateCommand.title),
                        userId = updateCommand.user.id
                    )
                )
            is CollectionUpdateCommand.ChangeVisibility ->
                eventBus.publish(
                    msg(
                        builder = CollectionVisibilityChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .isPublic(updateCommand.isPublic),
                        userId = updateCommand.user.id
                    )
                )
            is CollectionUpdateCommand.ReplaceSubjects, is CollectionUpdateCommand.RemoveSubjectFromCollection ->
                eventBus.publish(
                    msg(
                        builder = CollectionSubjectsChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .subjects(collection.subjects.map { it.id.value }.toMutableSet()),
                        userId = updateCommand.user.id
                    )
                )
            is CollectionUpdateCommand.ChangeAgeRange ->
                eventBus.publish(
                    msg(
                        builder = CollectionAgeRangeChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .rangeMin(updateCommand.minAge)
                            .rangeMax(updateCommand.maxAge),
                        userId = updateCommand.user.id
                    )
                )

            is CollectionUpdateCommand.ChangeDescription -> eventBus.publish(
                msg(
                    builder = CollectionDescriptionChanged.builder()
                        .collectionId(updateCommand.collectionId.value)
                        .description(updateCommand.description),
                    userId = updateCommand.user.id
                )
            )
            is CollectionUpdateCommand.AddAttachment -> null
            is CollectionUpdateCommand.BulkUpdateCollectionVideos -> eventBus.publish(
                msg(
                    builder = CollectionVideosBulkChanged.builder()
                        .collectionId(updateCommand.collectionId.value)
                        .videoIds(updateCommand.videoIds.map { it.value }),
                    userId = updateCommand.user.id
                )
            )
            is CollectionUpdateCommand.Bookmark -> eventBus.publish(
                msg(
                    CollectionBookmarkChanged.builder().collectionId(updateCommand.collectionId.value).isBookmarked(true),
                    userId = updateCommand.user.id
                )
            )
            is CollectionUpdateCommand.Unbookmark -> eventBus.publish(
                msg(
                    builder = CollectionBookmarkChanged.builder().collectionId(updateCommand.collectionId.value)
                        .isBookmarked(false),
                    userId = updateCommand.user.id
                )
            )
        }
    }

    fun publishVideoInteractedWithEvent(videoId: VideoId, subtype: String, user: User) {
        eventBus.publish(
            msg(
                builder = VideoInteractedWith.builder().videoId(videoId.value).subtype(subtype),
                userId = user.id
            )
        )
    }

    fun savePlaybackEvent(
        videoId: VideoId,
        videoIndex: Int?,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        playbackDevice: String?,
        user: User,
        timestamp: ZonedDateTime
    ) {
        eventBus.publish(
            msg(
                builder = VideoSegmentPlayed.builder()
                    .videoId(videoId.value)
                    .videoIndex(videoIndex)
                    .segmentStartSeconds(segmentStartSeconds)
                    .segmentEndSeconds(segmentEndSeconds)
                    .playbackDevice(playbackDevice)
                    .timestamp(timestamp),
                userId = user.id
            )
        )
    }

    fun savePlayerInteractedWithEvent(
        videoId: VideoId,
        currentTime: Long,
        subtype: String,
        payload: Map<String, Any>,
        user: User
    ) {
        eventBus.publish(
            msg(
                builder = VideoPlayerInteractedWith.builder()
                    .videoId(videoId.value)
                    .currentTime(currentTime)
                    .subtype(subtype)
                    .payload(payload),
                userId = user.id
            )
        )
    }

    fun saveCollectionInteractedWithEvent(
        collectionId: String,
        subtype: CollectionInteractionType,
        user: User
    ) {
        eventBus.publish(
            msg(
                CollectionInteractedWith.builder()
                    .collectionId(collectionId)
                    .subtype(subtype),
                user.id
            )
        )
    }

    private fun msg(
        builder: AbstractEventWithUserId.AbstractEventWithUserIdBuilder<*, *>,
        userId: UserId
    ): AbstractEventWithUserId {
        return builder
            .userId(userId.value)
            .url(RefererHeaderExtractor.getReferer())
            .build()
    }
}
