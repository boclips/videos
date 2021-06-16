package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class DeleteVideoThumbnail(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(videoId: String) {
        videoRepository.bulkUpdate(
            listOf(
                VideoUpdateCommand.ReplaceThumbnailSecond(
                    VideoId(videoId), null
                ),
                VideoUpdateCommand.ReplaceCustomThumbnail(
                    VideoId(videoId), null
                )
            )
        )
    }
}
