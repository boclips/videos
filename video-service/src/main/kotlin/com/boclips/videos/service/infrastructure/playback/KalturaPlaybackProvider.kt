package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import mu.KLogging

class KalturaPlaybackProvider(private val kalturaClient: KalturaClient) : PlaybackProvider {
    companion object : KLogging()

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, StreamPlayback> {
        val kalturaVideoIds = playbackIds.map { playbackId -> playbackId.value }
        val mediaEntriesById = kalturaClient.getMediaEntriesByReferenceIds(kalturaVideoIds)

        return playbackIds
                .asSequence()
                .filter { id ->
                    val kalturaVideoId = id.value
                    if (mediaEntriesById[kalturaVideoId] == null) {
                        logger.warn { "Omitted asset $kalturaVideoId due to lack of asset playback information" }
                        false
                    } else {
                        true
                    }
                }
                .map { id ->
                    val kalturaVideoId = id
                    val mediaEntry = mediaEntriesById[kalturaVideoId.value]!!.first()

                    val streamUrl = mediaEntry.streams.withFormat(StreamFormat.APPLE_HDS)
                    val videoPlayback = StreamPlayback(
                            id = id,
                            streamUrl = streamUrl,
                            thumbnailUrl = mediaEntry.thumbnailUrl,
                            duration = mediaEntry.duration
                    )

                    (id to videoPlayback)
                }
                .toMap()
    }

    override fun removePlayback(playbackId: PlaybackId) {
        try {
            kalturaClient.deleteMediaEntriesByReferenceId(playbackId.value)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to execute asset from Kaltura: $ex" }
        }
    }
}