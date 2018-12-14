package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.VideoSubjectsUpdate
import com.boclips.videos.service.domain.model.VideoTitleUpdate
import com.boclips.videos.service.domain.model.VideoUpdateCommand
import com.boclips.videos.service.domain.model.asset.Subject

class VideoResourceToVideoUpdateConverter {
    companion object {
        fun convert(videoResource: VideoResource): List<VideoUpdateCommand> {
            return listOfNotNull(
                    videoResource.title?.let { VideoTitleUpdate(it) },
                    videoResource.subjects?.let { VideoSubjectsUpdate(it.map { Subject(it) }.toSet()) }
            )
        }
    }
}
