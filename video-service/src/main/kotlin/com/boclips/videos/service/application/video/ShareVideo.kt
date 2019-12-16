package com.boclips.videos.service.application.video

import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class ShareVideo(
    private val userServiceClient: UserServiceClient,
    private val videoRepository: VideoRepository
) {
    operator fun invoke(videoId: String, user: User) {
        if (!user.isPermittedToShareVideo) {
            throw OperationForbiddenException()
        }
        val shareCode = userServiceClient.findUser(user.id.value).teacherPlatformAttributes.shareCode

        videoRepository.update(VideoUpdateCommand.AddShareCode(VideoId(videoId), shareCode))
    }
}