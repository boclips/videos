package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

open class VideoPlaybackService(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging() {
        const val KALTURA_FILTER = "kaltura"
        const val YOUTUBE_FILTER = "youtube"
    }

    open fun requestUpdate(source: String? = null) {
        logger.info("Requesting video playback synchronization for all videos")

        val filter = getFilter(source)

        try {
            when (filter) {
                null -> videoRepository.streamAll(publishToTopic())
                else -> videoRepository.streamAll(filter, publishToTopic())
            }
        } catch (ex: Exception) {
            logger.error { "Failed to request playback synchronization for (some) videos" }
        }
    }

    private fun publishToTopic(): (Sequence<Video>) -> Unit {
        return { sequence ->
            sequence.forEach { video ->
                val videoToBeUpdated = VideoPlaybackSyncRequested.builder()
                    .videoId(video.videoId.value)
                    .build()

                eventBus.publish(videoToBeUpdated)
                logger.info { "Playback synchronization requested for video ${video.videoId}" }
            }
        }
    }

    private fun getFilter(source: String?): VideoFilter? {
        return when (source) {
            KALTURA_FILTER -> VideoFilter.IsKaltura
            YOUTUBE_FILTER -> VideoFilter.IsYoutube
            null -> null
            else -> throw InvalidSourceException(source, listOf(KALTURA_FILTER, YOUTUBE_FILTER))
        }
    }

    @BoclipsEventListener
    fun updateVideoPlayback(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        try {
            handleUpdate(VideoId(value = videoPlaybackSyncRequestedEvent.videoId))
        } catch (ex: Exception) {
            logger.info { "Failed to process playback synchronization request for video ${videoPlaybackSyncRequestedEvent.videoId}: $ex" }
        }
    }

    private fun handleUpdate(videoId: VideoId) {
        val video = videoRepository.find(videoId)

        if (video == null) {
            logger.info { "Could find video $videoId" }
            return
        }

        if (!video.isPlayable()) {
            logger.info { "Video $videoId has no playback information associated with it." }
            return
        }

        val playback = playbackRepository.find(video.playback.id)
        if (playback == null) {
            logger.info { "Could not find playback information for $videoId (Playback Id: ${video.playback.id})" }
            return
        }

        val replacePlayback = VideoUpdateCommand.ReplacePlayback(
            videoId = video.videoId,
            playback = playback
        )

        try {
            videoRepository.update(replacePlayback)
            logger.info { "Updated playback information for video ${video.videoId} successfully" }
        } catch (ex: Exception) {
            logger.info { "Did not update playback for video ${video.videoId}: $ex" }
        }
    }
}
