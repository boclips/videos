package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.VideoResource

class VideoUpdatesConverter {
    companion object {
        fun convert(assetId: AssetId, videoResource: VideoResource): List<VideoUpdateCommand> {
            return if (videoResource.subjects != null) {
                val subjects = videoResource.subjects.map { LegacySubject(it) }.toList()
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
