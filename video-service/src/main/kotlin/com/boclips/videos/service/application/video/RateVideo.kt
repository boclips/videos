package com.boclips.videos.service.application.video

import com.boclips.security.utils.User
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.common.UserId
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

    open operator fun invoke(
        @Valid rateVideoRequest: RateVideoRequest, requester: User
    ) {
        if (!requester.hasRole(UserRoles.RATE_VIDEOS)) throw OperationForbiddenException()

        videoRepository.update(
            VideoUpdateCommand.AddRating(
                VideoId(rateVideoRequest.videoId),
                UserRating(rating = rateVideoRequest.rating!!, userId = UserId(requester.id))
            )
        )
    }
}
