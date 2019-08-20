package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.security.access.annotation.Secured
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.validation.annotation.Validated

@Validated
open class UpdateVideo(private val videoRepository: VideoRepository) {

    companion object : KLogging();

    open operator fun invoke(id: String, title: String?) {
        if(getCurrentUser().hasRole(UserRoles.UPDATE_VIDEOS).not()) throw OperationForbiddenException()

        videoRepository.update(VideoUpdateCommand.ReplaceTitle(VideoId(id), title!!))
        logger.info { "Updated title of video $id" }
    }
}
