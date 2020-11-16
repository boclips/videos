package com.boclips.videos.service.application.video

import com.boclips.videos.api.response.video.VideoUrlAssetsResource
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.InsufficientVideoResolutionException
import com.boclips.videos.service.domain.model.video.Video
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
        val playbackId = searchVideo.byId(videoId, user).let { video ->
            validateVideoIsDownloadable(video)
            video.playback.id
        }

        val videoAssetUrl = playbackProvider.getDownloadAssetUrl(playbackId)
        val captionUrl = playbackProvider.getHumanGeneratedCaptionUrl(playbackId)

        return VideoUrlAssetsResource(
            downloadVideoUrl = videoAssetUrl.toString(),
            downloadCaptionUrl = captionUrl?.toString()
        )
    }

    private fun validateVideoIsDownloadable(video: Video) {
        if (video.playback is VideoPlayback.StreamPlayback) {
            if (!video.playback.hasOriginalOrFHDResolution()) throw InsufficientVideoResolutionException(video.videoId)
        } else {
            throw VideoPlaybackNotFound("The requested video cannot be downloaded because it comes from an incompatible source")
        }
    }
}
