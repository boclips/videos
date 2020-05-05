package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.plackback.PlaybackService
import mu.KLogging

open class UpdateYoutubePlayback(
    private val playbackService: PlaybackService
) {
    companion object : KLogging()

    open fun updateYoutubeVideosPlayback() {
        playbackService.updatePlaybackFor(filter = VideoFilter.IsYoutube)
    }
}
