package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.projections.BoclipsInternalView
import com.boclips.videos.service.presentation.projections.PublicApiView
import com.boclips.videos.service.presentation.projections.TeachersView
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.LocalDate

@Relation(collectionRelation = "videos")
data class VideoResource(
    @get:JsonView(PublicApiView::class)
    val id: String? = null,
    @get:JsonView(PublicApiView::class)
    val title: String? = null,
    @get:JsonView(PublicApiView::class)
    val description: String? = null,
    @get:JsonView(PublicApiView::class)
    val releasedOn: LocalDate? = null,
    @get:JsonView(PublicApiView::class)
    val playback: Resource<PlaybackResource>? = null,
    @get:JsonView(PublicApiView::class)
    val subjects: Set<String>? = null,
    @get:JsonView(PublicApiView::class)
    val badges: Set<String> = emptySet(),
    @get:JsonView(PublicApiView::class)
    val legalRestrictions: String? = null,
    @get:JsonView(PublicApiView::class)
    val ageRange: AgeRangeResource? = null,
    @get:JsonView(PublicApiView::class)
    val rating: Int? = null,

    @get:JsonView(TeachersView::class)
    val contentPartner: String? = null,

    @get:JsonView(BoclipsInternalView::class)
    val type: VideoTypeResource? = null,
    @get:JsonView(BoclipsInternalView::class)
    val contentPartnerVideoId: String? = null,
    @get:JsonView(BoclipsInternalView::class)
    val hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource> = emptySet(),
    @get:JsonView(BoclipsInternalView::class)
    val status: VideoResourceStatus? = null,

    @get:JsonIgnore
    val hasTranscripts: Boolean? = null
)