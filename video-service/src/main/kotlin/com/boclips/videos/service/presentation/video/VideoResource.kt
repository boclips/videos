package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.LocalDate

@Relation(collectionRelation = "videos")
data class VideoResource(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val releasedOn: LocalDate? = null,
    val contentPartner: String? = null,
    val contentPartnerVideoId: String? = null,
    val playback: Resource<PlaybackResource>? = null,
    val subjects: Set<String>? = null,
    val badges: Set<String> = emptySet(),
    val type: VideoTypeResource? = null,
    val status: VideoResourceStatus? = null,
    val legalRestrictions: String? = null,
    @get:JsonIgnore
    val hasTranscripts: Boolean? = null
)