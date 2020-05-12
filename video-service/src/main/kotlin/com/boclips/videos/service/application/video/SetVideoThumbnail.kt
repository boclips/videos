package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.SetThumbnailRequest
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class SetVideoThumbnail(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(request: SetThumbnailRequest) {
        videoRepository.update(
            VideoUpdateCommand.ReplaceThumbnailSecond(
                VideoId(request.videoId), request.thumbnailSecond!!
            )
        )
    }
}