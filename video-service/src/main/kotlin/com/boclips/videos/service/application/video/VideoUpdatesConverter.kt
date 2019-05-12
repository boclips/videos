package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.VideoResource

class VideoUpdatesConverter {
    companion object {
        fun convert(videoId: VideoId, videoResource: VideoResource): List<VideoUpdateCommand> {
            return if (videoResource.subjects != null) {
                val subjects = videoResource.subjects.map { LegacySubject(it) }.toList()
                listOf(
                    VideoUpdateCommand.ReplaceSubjects(
                        videoId = videoId,
                        subjects = subjects
                    )
                )
            } else {
                emptyList()
            }
        }
    }
}
