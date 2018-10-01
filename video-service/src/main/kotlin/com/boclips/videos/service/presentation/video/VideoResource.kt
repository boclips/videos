package com.boclips.videos.service.presentation.video

import org.springframework.hateoas.core.Relation
import java.time.Duration
import java.time.LocalDate

@Relation(collectionRelation = "videos")
data class VideoResource(
        val id: String? = null,
        val title: String? = null,
        val description: String? = null,
        val duration: Duration? = null,
        val releasedOn: LocalDate? = null,
        val contentProvider: String? = null,
        val streamUrl: String? = null,
        val thumbnailUrl: String? = null
)