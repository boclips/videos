package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionUpdateCommand {
    data class AddVideoToCollection(val videoId: VideoId) : CollectionUpdateCommand()
    data class RemoveVideoFromCollection(val videoId: VideoId) : CollectionUpdateCommand()
    data class RenameCollection(val title: String) : CollectionUpdateCommand()
    data class ChangeVisibility(val isPublic: Boolean) : CollectionUpdateCommand()
    data class ReplaceSubjects(val subjects: Set<Subject>) : CollectionUpdateCommand()
    data class ChangeAgeRange(val minAge: Int, val maxAge: Int?) : CollectionUpdateCommand()
    data class RemoveSubjectFromCollection(val subjectId: SubjectId) : CollectionUpdateCommand()
}