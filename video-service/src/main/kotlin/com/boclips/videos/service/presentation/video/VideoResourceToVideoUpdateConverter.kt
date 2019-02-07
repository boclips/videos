package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.VideoSubjectsUpdate
import com.boclips.videos.service.domain.service.VideoUpdateIntent

class VideoResourceToVideoUpdateConverter {
    companion object {
        fun convert(videoResource: VideoResource): List<VideoUpdateIntent> {
            return listOfNotNull(
                    videoResource.subjects?.let { VideoSubjectsUpdate(it.map { Subject(it) }.toSet()) }
            )
        }
    }
}
