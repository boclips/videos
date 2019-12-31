package com.boclips.videos.api.request.video

import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonView
import java.time.Duration

sealed class PlaybackResource {
    @get:JsonView(PublicApiProjection::class)
    abstract var id: String?
    @get:JsonView(PublicApiProjection::class)
    abstract var thumbnailUrl: String?
    @get:JsonView(PublicApiProjection::class)
    abstract var duration: Duration?
    @get:JsonView(PublicApiProjection::class)
    abstract val type: String

    abstract val downloadUrl: String?
}

data class StreamPlaybackResource(
    @get:JsonView(PublicApiProjection::class)
    override val type: String = "STREAM",
    @get:JsonView(PublicApiProjection::class)
    override var id: String?,
    @get:JsonView(PublicApiProjection::class)
    override var thumbnailUrl: String?,
    @get:JsonView(PublicApiProjection::class)
    override var duration: Duration?,
    @get:JsonView(PublicApiProjection::class)
    val streamUrl: String,
    @get:JsonView(BoclipsInternalProjection::class)
    val referenceId: String,
    @get:JsonIgnore
    override val downloadUrl: String?
) : PlaybackResource()

data class YoutubePlaybackResource(
    @get:JsonView(PublicApiProjection::class)
    override val type: String = "YOUTUBE",
    @get:JsonView(PublicApiProjection::class)
    override var id: String?,
    @get:JsonView(PublicApiProjection::class)
    override var thumbnailUrl: String?,
    @get:JsonView(PublicApiProjection::class)
    override var duration: Duration?
) : PlaybackResource() {
    override val downloadUrl: String?
        get() = null
}
