package com.boclips.search.service.domain.videos.model

data class SubjectsMetadata(
    val items: Set<SubjectMetadata>,
    val setManually: Boolean?
)
