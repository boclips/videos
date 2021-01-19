package com.boclips.videos.service.application.video

import com.boclips.videos.api.response.video.VideoUrlAssetsResource
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.NoVideoAssetsException
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import mu.KLogging

class GetVideoUrlAssets(
    private val searchVideo: SearchVideo,
    private val playbackProvider: PlaybackProvider
) {
    companion object : KLogging()

    operator fun invoke(
        videoId: String,
        user: User
    ): VideoUrlAssetsResource {
        val videoPlayback = searchVideo.byId(videoId, user).let { video: BaseVideo ->
            validateVideoIsDownloadable(video)
            video.playback as VideoPlayback.StreamPlayback
        }

        val videoAssetUrl = videoPlayback
            .takeIf { it.hasOriginalOrFHDResolution() }
            ?.let { playbackProvider.getDownloadAssetUrl(videoPlayback.id) }

        val downloadableCaption = playbackProvider.getHumanGeneratedCaption(videoPlayback.id)

        return VideoUrlAssetsResource(
            downloadVideoUrl = videoAssetUrl?.toString(),
            downloadCaptionUrl = downloadableCaption?.downloadUrl?.toString(),
            captionFileExtension = downloadableCaption?.format?.getFileExtension()
        )
    }

    private fun validateVideoIsDownloadable(video: BaseVideo) {
        if (video.playback is VideoPlayback.StreamPlayback) {
            if ((video.playback as VideoPlayback.StreamPlayback).hasAnyAssets()) throw NoVideoAssetsException(video.videoId)
        } else {
            throw VideoPlaybackNotFound("The requested video cannot be downloaded because it comes from an incompatible source")
        }
    }
}
