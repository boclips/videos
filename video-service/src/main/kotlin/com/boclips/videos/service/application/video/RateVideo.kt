package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.RateVideoRequest
import mu.KLogging
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
open class RateVideo(
    private val videoRepository: VideoRepository
) {

    companion object : KLogging();

    open operator fun invoke(@Valid rateVideoRequest: RateVideoRequest) {
        videoRepository.update(
            VideoUpdateCommand.ReplaceRating(
                VideoId(rateVideoRequest.videoId),
                UserRating(rating = rateVideoRequest.rating!!, userId = getCurrentUserId())
            )
        )
    }
}
