package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.InsufficientVideoResolutionException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoUrlAsset
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import mu.KLogging
import java.net.URI

class GetVideoUrlAssets(
    private val captionService: CaptionService,
    private val searchVideo: SearchVideo,
    private val playbackProvider: PlaybackProvider
) {
    companion object : KLogging()

    operator fun invoke(
        videoId: String,
        user: User
    ): VideoUrlAsset {
        val playbackId = searchVideo.byId(videoId, user).let { video ->
            validateVideoIsDownloadable(video)
            video.playback.id
        }

        val videoAssetUrl = playbackProvider.getDownloadAssetUrl(playbackId)

        return VideoUrlAsset(downloadVideoUrl = videoAssetUrl.path)
    }

    private fun validateVideoIsDownloadable(
        video: Video
    ) {
        if (video.playback is VideoPlayback.StreamPlayback) {
            if (!video.playback.hasOriginalOrFHDResolution()) throw InsufficientVideoResolutionException(video.videoId)
        } else {
            throw VideoPlaybackNotFound("The requested video cannot be downloaded because it comes from an incompatible source")
        }
    }
}
