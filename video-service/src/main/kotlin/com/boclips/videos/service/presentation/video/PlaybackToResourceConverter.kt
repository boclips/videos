package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.UriTemplate
import org.springframework.stereotype.Component

@Component
class PlaybackToResourceConverter(
    val eventsLinkBuilder: EventsLinkBuilder,
    val playbacksLinkBuilder: PlaybacksLinkBuilder
) {
    fun wrapPlaybackInResource(playback: VideoPlayback): Resource<PlaybackResource> =
        Resource(
            toResource(playback),
            listOfNotNull(
                this.eventsLinkBuilder.createPlaybackEventLink(),
                this.eventsLinkBuilder.createPlayerInteractedWithEventLink(),
                this.playbacksLinkBuilder.downloadLink(playback),
                this.playbacksLinkBuilder.thumbnailLink(playback),
                this.playbacksLinkBuilder.videoPreviewLink(playback),
                this.playbacksLinkBuilder.hlsStreamLink(playback)
            )
        )

    private fun toResource(playback: VideoPlayback): PlaybackResource =
        when (playback) {
            is VideoPlayback.StreamPlayback -> {
                val thumbnailUrl = this.playbacksLinkBuilder.thumbnailLink(playback)!!.href
                val streamUrl = this.playbacksLinkBuilder.hlsStreamLink(playback)!!.href

                StreamPlaybackResource(
                    streamUrl = streamUrl,
                    thumbnailUrl = UriTemplate(thumbnailUrl).expand(mapOf(Pair("thumbnailWidth", 500))).toString(),
                    downloadUrl = playback.downloadUrl,
                    duration = playback.duration,
                    id = playback.id.value,
                    referenceId = playback.referenceId
                )
            }
            is VideoPlayback.YoutubePlayback -> YoutubePlaybackResource(
                thumbnailUrl = playback.thumbnailUrl,
                duration = playback.duration,
                id = playback.id.value
            )
            else -> throw Exception()
        }
}
