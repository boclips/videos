package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.application.video.RequestVideoPlaybackUpdate
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class UpdateYoutubeChannelNames(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val createOrUpdateContentPartner: CreateOrUpdateContentPartner
) {
    companion object : KLogging()

    operator fun invoke() {
        videoRepository.streamAll(VideoFilter.IsYoutube) { sequence -> sequence.forEach { handleUpdate(it) } }
    }

    private fun handleUpdate(videoToUpdate: Video) {
        val actualVideo = videoRepository.find(videoToUpdate.videoId)

        if (actualVideo == null) {
            logger.info { "Could find video $videoToUpdate" }
            return
        }

        val playback = playbackRepository.find(actualVideo.playback.id)
        if (playback == null) {
            RequestVideoPlaybackUpdate.logger.info { "Could not find playback information for $videoToUpdate (Playback Id: ${actualVideo.playback.id})" }
            return
        }

        val playbackProviderMetadata = playbackRepository.getProviderMetadata(actualVideo.playback.id)
        if (playbackProviderMetadata == null) {
            RequestVideoPlaybackUpdate.logger.info { "Could not find provider metadata information for $videoToUpdate (Playback Id: ${actualVideo.playback.id}" }
            return
        }

        if (playbackProviderMetadata.id.type == PlaybackProviderType.YOUTUBE) {
            updateContentPartnerWithChannelName(
                playbackProviderMetadata as VideoProviderMetadata.YoutubeMetadata,
                actualVideo
            )
        }
    }

    private fun updateContentPartnerWithChannelName(
        playbackProviderMetadata: VideoProviderMetadata.YoutubeMetadata,
        video: Video
    ) {
        val contentPartner = createOrUpdateContentPartner(
            contentPartnerId = ContentPartnerId(playbackProviderMetadata.channelId),
            provider = playbackProviderMetadata.channelName
        )

        val replaceContentPartnerCommand =
            VideoUpdateCommand.ReplaceContentPartner(videoId = video.videoId, contentPartner = contentPartner)

        try {
            videoRepository.update(replaceContentPartnerCommand)
            RequestVideoPlaybackUpdate.logger.info { "Updated content partner for video ${video.videoId} with content partner ${contentPartner.name}" }
        } catch (ex: Exception) {
            RequestVideoPlaybackUpdate.logger.info { "Did not update content partner for ${video.videoId}: $ex" }
        }
    }
}