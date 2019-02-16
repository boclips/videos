package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.video.ReplaceSubjects
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.VideoResource

class VideoUpdatesConverter {
    companion object {
        fun convert(videoResource: VideoResource): List<VideoUpdateCommand> {
            val videoId = AssetId(value = videoResource.id!!)

            return if (videoResource.subjects != null) {
                val subjects = videoResource.subjects.map { Subject(it) }.toList()
                listOf(
                    ReplaceSubjects(
                        assetId = videoId,
                        subjects = subjects
                    )
                )
            } else {
                emptyList()
            }
        }
    }
}
