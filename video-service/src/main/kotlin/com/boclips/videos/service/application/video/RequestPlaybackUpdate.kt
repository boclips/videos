package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder

open class RequestPlaybackUpdate(
    private val videoRepository: VideoRepository,
    private val topics: Topics
) {
    companion object : KLogging() {
        const val KALTURA_FILTER = "kaltura"
        const val YOUTUBE_FILTER = "youtube"
    }

    open operator fun invoke(source: String? = null) {
        logger.info("Requesting video playback synchronization for all videos")

        validateSource(source)

        try {
            when (source) {
                KALTURA_FILTER -> videoRepository.streamAll(VideoFilter.IsKaltura, publishToTopic())
                YOUTUBE_FILTER -> videoRepository.streamAll(VideoFilter.IsYoutube, publishToTopic())
                else -> videoRepository.streamAll(publishToTopic())
            }
        } catch (ex: Exception) {
            logger.error { "Failed to publish (some) events to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
        }
    }

    private fun publishToTopic(): (Sequence<Video>) -> Unit {
        return { sequence ->
            sequence.forEach { video ->
                val videoToBeUpdated = VideoPlaybackSyncRequested.builder()
                    .videoId(video.videoId.value)
                    .build()

                topics.videoPlaybackSyncRequested().send(MessageBuilder.withPayload(videoToBeUpdated).build())
                logger.info { "Video ${video.videoId} published to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
            }
        }
    }

    private fun validateSource(source: String?) {
        val validSources = listOf(YOUTUBE_FILTER, KALTURA_FILTER)

        if (source != null && !validSources.contains(source.toLowerCase())) {
            throw InvalidSourceException(source, validSources)
        }
    }
}
