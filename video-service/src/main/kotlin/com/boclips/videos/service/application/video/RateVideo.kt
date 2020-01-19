package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.RateVideoRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
open class RateVideo(
    private val videoRepository: VideoRepository
) {

    companion object : KLogging();

    open operator fun invoke(@Valid rateVideoRequest: RateVideoRequest, user: User) {
        if (!user.isPermittedToRateVideos) throw OperationForbiddenException()

        videoRepository.update(
            VideoUpdateCommand.AddRating(
                VideoId(rateVideoRequest.videoId),
                UserRating(rating = rateVideoRequest.rating!!, userId = user.id)
            )
        )
    }
}
