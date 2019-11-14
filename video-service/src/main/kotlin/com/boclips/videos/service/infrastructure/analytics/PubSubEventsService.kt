package com.boclips.videos.service.infrastructure.analytics

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.base.AbstractEventWithUserId
import com.boclips.eventbus.events.collection.CollectionAgeRangeChanged
import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.eventbus.events.collection.CollectionDeleted
import com.boclips.eventbus.events.collection.CollectionDescriptionChanged
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionSubjectsChanged
import com.boclips.eventbus.events.collection.CollectionVideosBulkChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.eventbus.events.video.VideoInteractedWith
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
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

    override fun saveUpdateCollectionEvent(updateCommands: List<CollectionUpdateCommand>) {
        updateCommands.forEach { saveUpdateCollectionEvent(it) }
    }

    override fun saveCollectionDeletedEvent(collectionId: CollectionId) {
        eventBus.publish(
            msg(
                CollectionDeleted.builder()
                    .collectionId(collectionId.value)
            )
        )
    }

    private fun saveUpdateCollectionEvent(updateCommand: CollectionUpdateCommand) {
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
            is CollectionUpdateCommand.ReplaceSubjects ->
                eventBus.publish(
                    msg(
                        CollectionSubjectsChanged.builder()
                            .collectionId(updateCommand.collectionId.value)
                            .subjects(updateCommand.subjects.map { it.id.value }.toMutableSet())
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
            is CollectionUpdateCommand.RemoveSubjectFromCollection -> eventBus.publish(
                msg(
                    CollectionSubjectsChanged.builder()
                        .collectionId(updateCommand.collectionId.value)
                        .subjects(emptySet())
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
                    CollectionBookmarkChanged.builder().collectionId(updateCommand.collectionId.value).isBookmarked(false)
                )
            )
        }
    }

    override fun publishVideoInteractedWithEvent(videoId: VideoId, subtype: String) {
        eventBus.publish(
            msg(
                VideoInteractedWith.builder().videoId(videoId.value).subtype(subtype)
            )
        )
    }

    override fun savePlaybackEvent(
        videoId: VideoId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
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
                    .playbackDevice(playbackDevice)
            )
        )
    }

    override fun savePlayerInteractedWithEvent(
        playerId: String,
        videoId: VideoId,
        currentTime: Long,
        subtype: String,
        payload: Map<String, Any>
    ) {
        eventBus.publish(
            msg(
                VideoPlayerInteractedWith.builder()
                    .playerId(playerId)
                    .videoId(videoId.value)
                    .currentTime(currentTime)
                    .subtype(subtype)
                    .payload(payload)
            )
        )
    }

    private fun msg(builder: AbstractEventWithUserId.AbstractEventWithUserIdBuilder<*, *>): AbstractEventWithUserId {
        val user = getCurrentUser()
        return builder
            .userId(user.id)
            .url(RefererHeaderExtractor.getReferer())
            .build()
    }
}
