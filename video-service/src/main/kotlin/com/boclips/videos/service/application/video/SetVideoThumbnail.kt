package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.SetThumbnailRequest
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class SetVideoThumbnail(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(request: SetThumbnailRequest) {
        val setThumbnailCommand: VideoUpdateCommand? = when (request) {
            is SetThumbnailRequest.SetCustomThumbnail -> VideoUpdateCommand.ReplaceCustomThumbnail(
                VideoId(request.videoId), true
            )
            is SetThumbnailRequest.SetThumbnailBySecond -> VideoUpdateCommand.ReplaceThumbnailSecond(
                VideoId(request.videoId), request.thumbnailSecond
            )
            else -> null
        }

        setThumbnailCommand?.let { videoRepository.update(it) }
    }
}
