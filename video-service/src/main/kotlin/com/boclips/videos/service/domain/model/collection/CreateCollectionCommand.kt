package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.UserId

data class CreateCollectionCommand(
    val owner: UserId,
    val title: String,
    val createdByBoclips: Boolean,
    val discoverable: Boolean,
    val description: String? = null,
    val subjects: Set<SubjectId> = emptySet(),
    val default: Boolean = false
)
