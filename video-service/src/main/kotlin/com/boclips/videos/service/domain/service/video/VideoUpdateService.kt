package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.UnsupportedVideoUpdateException
import com.boclips.videos.service.domain.model.video.Video

class VideoUpdateService(private val videoRepository: VideoRepository) {
    fun update(video: Video, updateCommands: List<VideoUpdateCommand>) {
        if (!video.isBoclipsHosted()) {
            updateCommands.forEach {
                when (it) {
                    is VideoUpdateCommand.ReplaceTitle -> throw UnsupportedVideoUpdateException(message = "Cannot update title of a Youtube video")
                    is VideoUpdateCommand.ReplaceDescription -> throw UnsupportedVideoUpdateException(message = "Cannot update description of a Youtube video")
                }
            }
        }

        videoRepository.bulkUpdate(updateCommands)
    }
}
