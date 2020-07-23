package com.boclips.videos.api.request.video

import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonView
import java.time.Duration

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = StreamPlaybackResource::class, name = "STREAM"),
    JsonSubTypes.Type(value = YoutubePlaybackResource::class, name = "YOUTUBE")
)
sealed class PlaybackResource {
    @get:JsonView(PublicApiProjection::class)
    abstract var id: String?
    @get:JsonView(PublicApiProjection::class)
    abstract var duration: Duration?
    @get:JsonView(PublicApiProjection::class)
    abstract val type: String
    abstract val _links: Map<String, HateoasLink>?
}

data class StreamPlaybackResource(
    @get:JsonView(PublicApiProjection::class)
    override val type: String = "STREAM",
    @get:JsonView(PublicApiProjection::class)
    override var id: String?,
    @get:JsonView(PublicApiProjection::class)
    override var duration: Duration? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    var referenceId: String?,
    @get:JsonView(BoclipsInternalProjection::class)
    val maxResolutionAvailable: Boolean? = null,
    @get:JsonIgnore
    val downloadUrl: String? = null,
    override val _links: Map<String, HateoasLink>? = null
) : PlaybackResource()

data class YoutubePlaybackResource(
    @get:JsonView(PublicApiProjection::class)
    override val type: String = "YOUTUBE",
    @get:JsonView(PublicApiProjection::class)
    override var id: String?,
    @get:JsonView(PublicApiProjection::class)
    override var duration: Duration? = null,
    override val _links: Map<String, HateoasLink>? = null
) : PlaybackResource()