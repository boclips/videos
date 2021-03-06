package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import com.boclips.videos.service.infrastructure.video.VideoAssetDocument
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
                thumbnailSecond = videoPlayback.thumbnailSecond,
                customThumbnail = videoPlayback.customThumbnail,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt(),
                assets = videoPlayback.assets?.map { convertAssetsToDocument(it) },
                originalWidth = videoPlayback.originalDimensions?.width,
                originalHeight = videoPlayback.originalDimensions?.height
            )
            is YoutubePlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                entryId = null,
                type = "YOUTUBE",
                thumbnailUrl = listOf(videoPlayback.thumbnailUrl),
                downloadUrl = null,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt(),
                assets = null,
                originalWidth = null,
                originalHeight = null,
                thumbnailSecond = null,
                customThumbnail = null
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
                    downloadUrl = playbackDocument.downloadUrl!!,
                    thumbnailSecond = playbackDocument.thumbnailSecond,
                    customThumbnail = playbackDocument.customThumbnail ?: false,
                    assets = playbackDocument.assets?.map { convertDocumentToAsset(it) }?.toSet(),
                    createdAt = null,
                    originalDimensions = convertOriginalDimensions(
                        width = playbackDocument.originalWidth,
                        height = playbackDocument.originalHeight
                    )
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

    private fun convertOriginalDimensions(width: Int?, height: Int?): Dimensions? {
        if (width != null && height != null) {
            return Dimensions(width, height)
        }
        return null
    }

    private fun convertAssetsToDocument(asset: VideoAsset): VideoAssetDocument {
        return VideoAssetDocument(
            id = asset.reference,
            sizeKb = asset.sizeKb,
            width = asset.dimensions.width,
            height = asset.dimensions.height,
            bitrateKbps = asset.bitrateKbps
        )
    }

    private fun convertDocumentToAsset(asset: VideoAssetDocument): VideoAsset {
        return VideoAsset(
            reference = asset.id!!,
            sizeKb = asset.sizeKb!!,
            dimensions = Dimensions(
                asset.width!!,
                asset.height!!
            ),
            bitrateKbps = asset.bitrateKbps!!
        )
    }
}
