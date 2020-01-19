package com.boclips.videos.api.request.video

import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.hateoas.Link
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
    abstract val downloadUrl: String?
    abstract val _links: Map<String, Link>?
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
    @get:JsonIgnore
    override val downloadUrl: String? = null,
    override val _links: Map<String, Link>? = null
) : PlaybackResource()

data class YoutubePlaybackResource(
    @get:JsonView(PublicApiProjection::class)
    override val type: String = "YOUTUBE",
    @get:JsonView(PublicApiProjection::class)
    override var id: String?,
    @get:JsonView(PublicApiProjection::class)
    override var duration: Duration? = null,
    override val _links: Map<String, Link>? = null
) : PlaybackResource() {
    override val downloadUrl: String?
        get() = null
}
