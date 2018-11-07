package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotDeleted
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import mu.KLogging

class KalturaPlaybackProvider(private val kalturaClient: KalturaClient) : PlaybackProvider {
    companion object : KLogging()

    override fun retrievePlayback(videoIds: List<String>): Map<String, StreamPlayback> {
        val mediaEntriesById = kalturaClient.getMediaEntriesByReferenceIds(videoIds)

        return videoIds
                .asSequence()
                .filter { id ->
                    if (mediaEntriesById[id] == null) {
                        logger.warn { "Omitted video $id due to lack of video playback information" }
                        false
                    } else {
                        true
                    }
                }
                .map { id ->
                    val mediaEntry = mediaEntriesById[id]!!.first()

                    val streamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH)
                    val videoPlayback = StreamPlayback(
                            streamUrl = streamUrl,
                            thumbnailUrl = mediaEntry.thumbnailUrl,
                            duration = mediaEntry.duration
                    )

                    (id to videoPlayback)
                }
                .toMap()
    }

    override fun removePlayback(videoId: String) {
        try {
            kalturaClient.deleteMediaEntriesByReferenceId(videoId)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to execute video from Kaltura: $ex" }
            throw VideoPlaybackNotDeleted()
        }
    }
}