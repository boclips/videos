package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotDeleted
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import mu.KLogging

class KalturaPlaybackProvider(private val kalturaClient: KalturaClient) : PlaybackProvider {
    companion object : KLogging()

    override fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        val referenceIds = videos.map { video -> video.playbackId.playbackId }
        val mediaEntriesById = kalturaClient.getMediaEntriesByReferenceIds(referenceIds)

        return videos
                .asSequence()
                .filter { video ->
                    val id = video.playbackId.playbackId
                    if (mediaEntriesById[id] == null) {
                        logger.warn { "Omitted video $id due to lack of video playback information" }
                        false
                    } else {
                        true
                    }
                }
                .map { video ->
                    val id = video.playbackId.playbackId
                    val mediaEntry = mediaEntriesById[id]!!.first()

                    val streamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH)
                    val videoPlayback = StreamPlayback(
                            streamUrl = streamUrl,
                            thumbnailUrl = mediaEntry.thumbnailUrl,
                            duration = mediaEntry.duration
                    )
                    video.copy(videoPlayback = videoPlayback)
                }
                .toList()

    }

    override fun removePlayback(video: Video) {
        try {
            kalturaClient.deleteMediaEntriesByReferenceId(video.playbackId.playbackId)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to execute video from Kaltura: $ex" }
            throw VideoPlaybackNotDeleted()
        }
    }
}