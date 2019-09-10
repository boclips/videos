package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.subject.SubjectId

sealed class CollectionFilter {
    data class HasSubjectId(val subjectId: SubjectId) : CollectionFilter()
}
