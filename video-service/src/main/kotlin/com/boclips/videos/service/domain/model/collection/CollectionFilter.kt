package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionFilter {
    data class HasSubjectId(val subjectId: SubjectId) : CollectionFilter()
    data class HasVideoId(val videoId: VideoId): CollectionFilter()
}
