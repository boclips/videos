package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.plackback.PlaybackUpdateService
import mu.KLogging

open class UpdateYoutubePlayback(
    private val playbackUpdateService: PlaybackUpdateService
) {
    companion object : KLogging()

    open operator fun invoke() {
        playbackUpdateService.updatePlaybacksFor(filter = VideoFilter.IsYoutube)
    }
}
