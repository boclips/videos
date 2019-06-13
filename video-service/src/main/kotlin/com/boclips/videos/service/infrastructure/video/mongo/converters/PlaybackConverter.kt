package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.infrastructure.video.mongo.PlaybackDocument
import mu.KLogging
import java.time.Duration
import java.time.Instant

object PlaybackConverter : KLogging() {

    fun toDocument(videoPlayback: VideoPlayback): PlaybackDocument {
        return when (videoPlayback) {
            is VideoPlayback.StreamPlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                type = "KALTURA",
                thumbnailUrl = listOf(videoPlayback.thumbnailUrl),
                downloadUrl = videoPlayback.downloadUrl,
                hlsStreamUrl = videoPlayback.appleHlsStreamUrl,
                dashStreamUrl = videoPlayback.mpegDashStreamUrl,
                progressiveStreamUrl = videoPlayback.progressiveDownloadStreamUrl,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt()
            )
            is VideoPlayback.YoutubePlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                type = "YOUTUBE",
                thumbnailUrl = listOf(videoPlayback.thumbnailUrl),
                downloadUrl = null,
                hlsStreamUrl = null,
                dashStreamUrl = null,
                progressiveStreamUrl = null,
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

                VideoPlayback.StreamPlayback(
                    id = PlaybackId(type = PlaybackProviderType.KALTURA, value = playbackDocument.id),
                    thumbnailUrl = playbackDocument.thumbnailUrl!!.first(),
                    duration = Duration.ofSeconds(playbackDocument.duration!!.toLong()),
                    appleHlsStreamUrl = playbackDocument.hlsStreamUrl!!,
                    mpegDashStreamUrl = playbackDocument.dashStreamUrl!!,
                    progressiveDownloadStreamUrl = playbackDocument.progressiveStreamUrl!!,
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

                VideoPlayback.YoutubePlayback(
                    id = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = playbackDocument.id),
                    thumbnailUrl = playbackDocument.thumbnailUrl!!.first(),
                    duration = Duration.ofSeconds(playbackDocument.duration!!.toLong())
                )
            }
            else -> throw java.lang.IllegalStateException("Could not find video provider ${playbackDocument.type}")
        }
    }
}