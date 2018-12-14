package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import org.springframework.hateoas.core.Relation
import java.time.LocalDate

@Relation(collectionRelation = "videos")
data class VideoResource(
        val id: String? = null,
        val title: String? = null,
        val description: String? = null,
        val releasedOn: LocalDate? = null,
        val contentPartner: String? = null,
        val playback: PlaybackResource? = null,
        val subjects: Set<String>? = null
)