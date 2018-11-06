package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotDeleted
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.PlaybackService
import mu.KLogging

class KalturaPlaybackService(private val kalturaClient: KalturaClient) : PlaybackService {
    companion object : KLogging()

    override fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        val referenceIds = videos.map { video -> video.videoId.referenceId ?: video.videoId.videoId }
        val mediaEntriesById = kalturaClient.getMediaEntriesByReferenceIds(referenceIds)

        return videos
                .asSequence()
                .filter { video ->
                    val id = video.videoId.referenceId ?: video.videoId.videoId
                    if (mediaEntriesById[id] == null) {
                        logger.warn { "Omitted video $id due to lack of video playback information" }
                        false
                    } else {
                        true
                    }
                }
                .map { video ->
                    val id = video.videoId.referenceId ?: video.videoId.videoId
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

    override fun getVideoWithPlayback(video: Video): Video {
        val id = video.videoId.referenceId ?: throw IllegalArgumentException("ReferenceId needed to execute playback")
        val mediaEntries = kalturaClient.getMediaEntriesByReferenceId(id)

        if (mediaEntries.isEmpty()) throw VideoPlaybackNotFound()

        val mediaEntry = mediaEntries.first()
        val streamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH)
        val thumbnailUrl = mediaEntry.thumbnailUrl
        val videoPlayback = StreamPlayback(streamUrl = streamUrl,
                thumbnailUrl = thumbnailUrl,
                duration = mediaEntry.duration)

        return video.copy(videoPlayback = videoPlayback)
    }

    override fun removePlayback(video: Video) {
        try {
            kalturaClient.deleteMediaEntriesByReferenceId(video.videoId.referenceId)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to execute video from Kaltura: $ex" }
            throw VideoPlaybackNotDeleted()
        }
    }
}