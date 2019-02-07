package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.VideoSubjectsUpdate

class VideoResourceToVideoUpdateConverter {
    companion object {
        fun convert(videoResource: VideoResource): VideoSubjectsUpdate {
            return videoResource.subjects.let { VideoSubjectsUpdate(it?.map { subject -> Subject(subject) }?.toList() ?: emptyList()) }
        }
    }
}
