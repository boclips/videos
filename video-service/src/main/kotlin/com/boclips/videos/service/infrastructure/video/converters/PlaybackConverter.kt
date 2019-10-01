package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import mu.KLogging
import java.time.Duration
import java.time.Instant

object PlaybackConverter : KLogging() {

    fun toDocument(videoPlayback: VideoPlayback): PlaybackDocument {
        return when (videoPlayback) {
            is StreamPlayback -> PlaybackDocument(
                id = videoPlayback.referenceId,
                entryId = videoPlayback.id.value,
                type = "KALTURA",
                downloadUrl = videoPlayback.downloadUrl,
                thumbnailUrl = null,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt()
            )
            is YoutubePlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                entryId = null,
                type = "YOUTUBE",
                thumbnailUrl = listOf(videoPlayback.thumbnailUrl),
                downloadUrl = null,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt()
            )
            else -> throw IllegalStateException("Stream class ${videoPlayback.javaClass.name} not supported.")
        }
    }

    fun toPlayback(playbackDocument: PlaybackDocument?): VideoPlayback {
        if (playbackDocument == null) {
            throw VideoPlaybackNotFound()
        }

        return when (playbackDocument.type) {
            PlaybackDocument.PLAYBACK_TYPE_KALTURA -> {
                if (!playbackDocument.isCompleteKalturaPlayback()) {
                    logger.info { "Failed to convert $playbackDocument" }

                    return VideoPlayback.FaultyPlayback(
                        id = PlaybackId(type = PlaybackProviderType.KALTURA, value = playbackDocument.id)
                    )
                }

                StreamPlayback(
                    id = PlaybackId(type = PlaybackProviderType.KALTURA, value = playbackDocument.entryId!!),
                    referenceId = playbackDocument.id,
                    duration = Duration.ofSeconds(playbackDocument.duration!!.toLong()),
                    downloadUrl = playbackDocument.downloadUrl!!
                )
            }
            PlaybackDocument.PLAYBACK_TYPE_YOUTUBE -> {
                if (!playbackDocument.isCompleteYoutubePlayback()) {
                    logger.info { "Failed to convert $playbackDocument" }

                    return VideoPlayback.FaultyPlayback(
                        id = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = playbackDocument.id)
                    )
                }

                YoutubePlayback(
                    id = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = playbackDocument.id),
                    duration = Duration.ofSeconds(playbackDocument.duration!!.toLong()),
                    thumbnailUrl = playbackDocument.thumbnailUrl!!.first()
                )
            }
            else -> throw java.lang.IllegalStateException("Could not find video provider ${playbackDocument.type}")
        }
    }
}
