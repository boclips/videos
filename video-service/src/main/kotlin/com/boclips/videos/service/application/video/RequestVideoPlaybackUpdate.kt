package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.application.contentPartner.CreateOrUpdateContentPartner
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata.YoutubeMetadata
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

open class RequestVideoPlaybackUpdate(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val createOrUpdateContentPartner: CreateOrUpdateContentPartner,
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

    @StreamListener(Subscriptions.VIDEO_PLAYBACK_SYNC_REQUESTED)
    operator fun invoke(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        try {
            handleUpdate(videoPlaybackSyncRequestedEvent)
        } catch (ex: Exception) {
            logger.info { "Failed to process ${Subscriptions.VIDEO_PLAYBACK_SYNC_REQUESTED} for ${videoPlaybackSyncRequestedEvent.videoId}: $ex" }
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

    private fun handleUpdate(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        val potentialVideoToBeUpdated = VideoId(value = videoPlaybackSyncRequestedEvent.videoId)
        val actualVideo = videoRepository.find(potentialVideoToBeUpdated)

        if (actualVideo == null) {
            logger.info { "Could find video $potentialVideoToBeUpdated" }
            return
        }

        if (!actualVideo.isPlayable()) {
            logger.info { "Video $potentialVideoToBeUpdated has no playback information associated with it." }
            return
        }

        val playback = playbackRepository.find(actualVideo.playback.id)
        if (playback == null) {
            logger.info { "Could not find playback information for $potentialVideoToBeUpdated (Playback Id: ${actualVideo.playback.id})" }
            return
        }

        val playbackProviderMetadata = playbackRepository.getProviderMetadata(actualVideo.playback.id)
        if (playbackProviderMetadata == null) {
            logger.info { "Could not find provider metadata information for $potentialVideoToBeUpdated (Playback Id: ${actualVideo.playback.id}" }
            return
        }

        if (playbackProviderMetadata.id.type == PlaybackProviderType.YOUTUBE) {
            updateContentPartnerWithChannelName(playbackProviderMetadata as YoutubeMetadata, actualVideo)
        }

        val replacePlayback = VideoUpdateCommand.ReplacePlayback(
            videoId = actualVideo.videoId,
            playback = playback
        )

        try {
            videoRepository.update(replacePlayback)
            logger.info { "Updated playback information for video ${actualVideo.videoId} successfully" }
        } catch (ex: Exception) {
            logger.info { "Did not update playback for video ${actualVideo.videoId}: $ex" }
        }
    }

    private fun updateContentPartnerWithChannelName(playbackProvderMetadata: YoutubeMetadata, video: Video) {
        val contentPartner = createOrUpdateContentPartner(
            contentPartnerId = ContentPartnerId(playbackProvderMetadata.channelId),
            provider = playbackProvderMetadata.channelName
        )

        val replaceContentPartnerCommand =
            VideoUpdateCommand.ReplaceContentPartner(videoId = video.videoId, contentPartner = contentPartner)
        videoRepository.update(replaceContentPartnerCommand)
    }

    private fun validateSource(source: String?) {
        val validSources = listOf(YOUTUBE_FILTER, KALTURA_FILTER)

        if (source != null && !validSources.contains(source.toLowerCase())) {
            throw InvalidSourceException(source, validSources)
        }
    }
}
