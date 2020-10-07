package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder

class PlaybackToResourceConverter(
    val eventsLinkBuilder: EventsLinkBuilder,
    val playbacksLinkBuilder: PlaybacksLinkBuilder
) {
    fun convert(
        playback: VideoPlayback,
        videoId: VideoId,
        omitProtectedAttributes: Boolean? = false
    ): PlaybackResource =
        when (omitProtectedAttributes) {
            true -> convertWithoutProtectedAttributes(playback)
            else ->
                when
                    (playback) {
                    is VideoPlayback.StreamPlayback -> {
                        StreamPlaybackResource(
                            downloadUrl = playback.downloadUrl,
                            duration = playback.duration,
                            id = playback.id.value,
                            referenceId = playback.referenceId,
                            maxResolutionAvailable = playback.hasOriginalOrFHDResolution(),
                            _links = links(playback, videoId).map { it.rel to it }.toMap()
                        )
                    }
                    is VideoPlayback.YoutubePlayback -> YoutubePlaybackResource(
                        duration = playback.duration,
                        id = playback.id.value,
                        _links = links(playback, videoId).map { it.rel to it }.toMap()
                    )
                    else -> throw Exception()
                }
        }

    fun links(videoPlayback: VideoPlayback, videoId: VideoId): List<HateoasLink> {
        return listOfNotNull(
            this.eventsLinkBuilder.createPlaybackEventLink(),
            this.eventsLinkBuilder.createPlayerInteractedWithEventLink(),
            this.playbacksLinkBuilder.downloadLink(videoPlayback),
            this.playbacksLinkBuilder.thumbnailLink(videoPlayback),
            this.playbacksLinkBuilder.setThumbnailBySecond(videoPlayback, videoId),
            this.playbacksLinkBuilder.setCustomThumbnail(videoPlayback, videoId),
            this.playbacksLinkBuilder.deleteThumbnail(videoPlayback, videoId),
            this.playbacksLinkBuilder.videoPreviewLink(videoPlayback),
            this.playbacksLinkBuilder.hlsStreamLink(videoPlayback)
        )
    }

    private fun convertWithoutProtectedAttributes(playback: VideoPlayback): PlaybackResource =
        when
            (playback) {
            is VideoPlayback.StreamPlayback -> {
                StreamPlaybackResource(
                    id = playback.id.value,
                    referenceId = playback.referenceId,
                    _links = nonProtectedLinks(playback).map { it.rel to it }.toMap()
                )
            }
            is VideoPlayback.YoutubePlayback -> YoutubePlaybackResource(
                id = playback.id.value,
                _links = nonProtectedLinks(playback).map { it.rel to it }.toMap()
            )
            else -> throw Exception()
        }

    private fun nonProtectedLinks(playback: VideoPlayback): List<HateoasLink> = listOfNotNull(
        this.playbacksLinkBuilder.thumbnailLink(playback)
    )
}
