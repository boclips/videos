package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotDeleted
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoPlayback
import com.boclips.videos.service.domain.service.PlaybackService
import mu.KLogging

class KalturaPlaybackService(private val kalturaClient: KalturaClient) : PlaybackService {
    companion object : KLogging()

    override fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        val referenceIds = videos.map { video -> video.videoId.referenceId ?: video.videoId.videoId }
        val mediaEntriesById = kalturaClient.getMediaEntriesByReferenceIds(referenceIds)

        return videos.map { video ->
            val id = video.videoId.referenceId ?: video.videoId.videoId
            val mediaEntries: MutableList<MediaEntry> = mediaEntriesById[id]
                    ?: throw VideoPlaybackNotFound()

            val mediaEntry = mediaEntries.first()
            val streamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH)
            val videoPlayback = VideoPlayback(
                    streamUrl = streamUrl,
                    thumbnailUrl = mediaEntry.thumbnailUrl,
                    duration = mediaEntry.duration
            )
            video.copy(videoPlayback = videoPlayback)
        }
    }

    override fun getVideoWithPlayback(video: Video): Video {
        val id = video.videoId.referenceId ?: throw IllegalArgumentException("ReferenceId needed to get playback")

        val mediaEntries = kalturaClient.getMediaEntriesByReferenceId(id)

        if (mediaEntries.isEmpty()) throw VideoPlaybackNotFound()

        val mediaEntry = mediaEntries.first()
        val streamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH)
        val thumbnailUrl = mediaEntry.thumbnailUrl

        return video
                .copy(videoPlayback = VideoPlayback(streamUrl = streamUrl,
                        thumbnailUrl = thumbnailUrl,
                        duration = mediaEntry.duration)
                )
    }

    override fun removePlayback(video: Video) {
        try {
            kalturaClient.deleteMediaEntriesByReferenceId(video.videoId.referenceId)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to delete video from Kaltura: $ex" }
            throw VideoPlaybackNotDeleted()
        }
    }
}