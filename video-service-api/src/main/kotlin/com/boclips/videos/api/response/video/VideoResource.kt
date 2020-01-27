package com.boclips.videos.api.response.video

import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.hateoas.Link
import java.time.LocalDate

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
    val playback: PlaybackResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val subjects: Set<SubjectResource> = emptySet(),
    @get:JsonView(PublicApiProjection::class)
    val badges: Set<String> = emptySet(),
    @get:JsonView(PublicApiProjection::class)
    val legalRestrictions: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val ageRange: AgeRangeResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val rating: Double? = null,
    @get:JsonView(PublicApiProjection::class)
    val yourRating: Double? = null,
    @get:JsonView(PublicApiProjection::class)
    val bestFor: List<TagResource>? = null,
    @get:JsonView(PublicApiProjection::class)
    val createdBy: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val promoted: Boolean? = null,
    @get:JsonView(PublicApiProjection::class)
    val language: LanguageResource? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val contentPartner: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val contentPartnerId: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val contentPartnerVideoId: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val type: VideoTypeResource? = null,
    @get:JsonIgnore
    val hasTranscripts: Boolean? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>?
)

