package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import org.bson.types.ObjectId
import org.litote.kmongo.newId

class VideoDuplicationService(
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository
) {
    fun markDuplicate(
        oldVideoId: VideoId,
        activeVideoId: VideoId,
        user: User
    ) {
        videoRepository.update(
            VideoUpdateCommand.MarkAsDuplicate(
                videoId = oldVideoId,
                activeVideoId = activeVideoId
            )
        )

        collectionRepository.streamUpdate(CollectionFilter.HasVideoId(oldVideoId), { collection ->
            CollectionUpdateCommand.ReplaceVideos(
                collectionId = collection.id,
                videoIds = collection.videos.map { if ( it == oldVideoId ) activeVideoId else it },
                user = user
            )
        }, {})
    }
}