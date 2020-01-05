package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import org.springframework.hateoas.Link
import org.springframework.hateoas.UriTemplate
import org.springframework.stereotype.Component

@Component
class PlaybackToResourceConverter(
    val eventsLinkBuilder: EventsLinkBuilder,
    val playbacksLinkBuilder: PlaybacksLinkBuilder
) {
    fun convert(playback: VideoPlayback): PlaybackResource = when (playback) {
        is VideoPlayback.StreamPlayback -> {
            val thumbnailUrl = playbacksLinkBuilder.thumbnailLink(playback)!!.href
            val streamUrl = playbacksLinkBuilder.hlsStreamLink(playback)!!.href

            StreamPlaybackResource(
                streamUrl = streamUrl,
                thumbnailUrl = UriTemplate(thumbnailUrl).expand(mapOf(Pair("thumbnailWidth", 500))).toString(),
                downloadUrl = playback.downloadUrl,
                duration = playback.duration,
                id = playback.id.value,
                referenceId = playback.referenceId,
                _links = links(playback).map { it.rel to it }.toMap()
            )
        }
        is VideoPlayback.YoutubePlayback -> YoutubePlaybackResource(
            thumbnailUrl = playback.thumbnailUrl,
            duration = playback.duration,
            id = playback.id.value,
            _links = links(playback).map { it.rel to it }.toMap()
        )
        else -> throw Exception()
    }

    fun links(videoPlayback: VideoPlayback): List<Link> {
        return listOfNotNull(
            this.eventsLinkBuilder.createPlaybackEventLink(),
            this.eventsLinkBuilder.createPlayerInteractedWithEventLink(),
            this.playbacksLinkBuilder.downloadLink(videoPlayback),
            this.playbacksLinkBuilder.thumbnailLink(videoPlayback),
            this.playbacksLinkBuilder.videoPreviewLink(videoPlayback),
            this.playbacksLinkBuilder.hlsStreamLink(videoPlayback)
        )
    }
}
