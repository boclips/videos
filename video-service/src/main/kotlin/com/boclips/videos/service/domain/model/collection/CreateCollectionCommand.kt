package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.subject.SubjectId

data class CreateCollectionCommand(
    val owner: UserId,
    val title: String,
    val createdByBoclips: Boolean,
    val public: Boolean,
    val description: String? = null,
    val subjects: Set<SubjectId> = emptySet()
)
