package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.VideoResource

class VideoUpdatesConverter {
    companion object {
        fun convert(assetId: AssetId, videoResource: VideoResource): List<VideoUpdateCommand> {
            return if (videoResource.subjects != null) {
                val subjects = videoResource.subjects.map { Subject(it) }.toList()
                listOf(
                    VideoUpdateCommand.ReplaceSubjects(
                        assetId = assetId,
                        subjects = subjects
                    )
                )
            } else {
                emptyList()
            }
        }
    }
}
