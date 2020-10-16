package com.boclips.search.service.domain.videos.model

import com.boclips.search.service.domain.subjects.model.SubjectMetadata

data class SubjectsMetadata(
    val items: Set<SubjectMetadata>,
    val setManually: Boolean?
)
