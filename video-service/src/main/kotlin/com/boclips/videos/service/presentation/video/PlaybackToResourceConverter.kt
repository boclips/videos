package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.UriTemplate
import org.springframework.stereotype.Component

@Component
class PlaybackToResourceConverter(
    val eventsLinkBuilder: EventsLinkBuilder
) {
    fun wrapPlaybackInResource(playback: VideoPlayback): Resource<PlaybackResource> {
        return wrapResourceWithHateoas(toResource(playback))
    }

    private fun wrapResourceWithHateoas(
        resource: PlaybackResource
    ) = Resource(
        resource,
        listOfNotNull(
            this.eventsLinkBuilder.createPlaybackEventLink(),
            this.eventsLinkBuilder.createPlayerInteractedWithEventLink()
        )
    )

    private fun toResource(playback: VideoPlayback): PlaybackResource {
        return when (playback) {
            is VideoPlayback.StreamPlayback -> StreamPlaybackResource(
                streamUrl = playback.appleHlsStreamUrl,
                thumbnailUrl = UriTemplate(playback.thumbnailUrl).expand(mapOf(Pair("thumbnailWidth", 500))).toString(),
                duration = playback.duration,
                id = playback.id.value,
                referenceId = playback.referenceId
            )
            is VideoPlayback.YoutubePlayback -> YoutubePlaybackResource(
                thumbnailUrl = playback.thumbnailUrl,
                duration = playback.duration,
                id = playback.id.value
            )
            else -> throw Exception()
        }
    }
}
