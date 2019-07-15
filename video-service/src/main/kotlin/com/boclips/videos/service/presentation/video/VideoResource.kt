package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.projections.BoclipsInternalProjection
import com.boclips.videos.service.presentation.projections.PublicApiProjection
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.LocalDate

@Relation(collectionRelation = "videos")
data class VideoResource(
    @get:JsonView(PublicApiProjection::class)
    val id: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val title: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val description: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val releasedOn: LocalDate? = null,
    @get:JsonView(PublicApiProjection::class)
    val playback: Resource<PlaybackResource>? = null,
    @get:JsonView(PublicApiProjection::class)
    val subjects: Set<String>? = null,
    @get:JsonView(PublicApiProjection::class)
    val badges: Set<String> = emptySet(),
    @get:JsonView(PublicApiProjection::class)
    val legalRestrictions: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val ageRange: AgeRangeResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val rating: Double? = null,
    @get:JsonView(PublicApiProjection::class)
    val source: String? = null,

    @get:JsonView(BoclipsInternalProjection::class)
    val contentPartner: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val contentPartnerVideoId: String? = null,

    @get:JsonView(BoclipsInternalProjection::class)
    val type: VideoTypeResource? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val distributionMethods: Set<DistributionMethodResource> = emptySet(),

    @get:JsonIgnore
    val hasTranscripts: Boolean? = null
)
