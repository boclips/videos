package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionsUpdateCommand {
    data class RemoveVideoFromAllCollections(val videoId: VideoId) : CollectionsUpdateCommand()
    data class RemoveSubjectFromAllCollections(val subjectId: SubjectId) : CollectionsUpdateCommand()
}