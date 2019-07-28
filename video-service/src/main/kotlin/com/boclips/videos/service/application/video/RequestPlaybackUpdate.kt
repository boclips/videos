package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging

open class RequestPlaybackUpdate(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus
) {
    companion object : KLogging() {
        const val KALTURA_FILTER = "kaltura"
        const val YOUTUBE_FILTER = "youtube"
    }

    open operator fun invoke(source: String? = null) {
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
}
