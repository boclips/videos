package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.VideoSubjectsUpdate
import com.boclips.videos.service.domain.service.VideoTitleUpdate
import com.boclips.videos.service.domain.service.VideoUpdateCommand

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
