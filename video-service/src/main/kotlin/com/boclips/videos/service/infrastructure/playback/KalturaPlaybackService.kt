package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.application.exceptions.VideoPlaybackNotDeleted
import com.boclips.videos.service.application.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoPlayback
import com.boclips.videos.service.domain.service.PlaybackService
import mu.KLogging

class KalturaPlaybackService(private val kalturaClient: KalturaClient) : PlaybackService {
    companion object : KLogging()

    override fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        val referenceIds = videos.map { video -> video.videoId.referenceId }
        val mediaEntries = kalturaClient.getMediaEntriesByReferenceIds(referenceIds)

        return videos.map { video ->
            val mediaEntry: MutableList<MediaEntry> = mediaEntries[video.videoId.referenceId]
                    ?: throw VideoPlaybackNotFound()

            val streamUrl = mediaEntry.first().streams.withFormat(StreamFormat.MPEG_DASH)
            video.copy(videoPlayback = VideoPlayback(streamUrl = streamUrl, thumbnailUrl = mediaEntry.first().thumbnailUrl))
        }
    }

    override fun getVideoWithPlayback(video: Video): Video {
        val referenceId = video.videoId.referenceId ?: throw VideoPlaybackNotFound()

        val mediaEntries = kalturaClient.getMediaEntriesByReferenceId(referenceId)

        if (mediaEntries.isEmpty()) throw VideoPlaybackNotFound()

        val streamUrl = mediaEntries.first().streams.withFormat(StreamFormat.MPEG_DASH)
        val thumbnailUrl = mediaEntries.first().thumbnailUrl

        return video.copy(videoPlayback = VideoPlayback(streamUrl = streamUrl, thumbnailUrl = thumbnailUrl))
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