package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.validation.annotation.Validated

@Validated
open class UpdateVideo(private val videoRepository: VideoRepository) {

    companion object : KLogging();

    open operator fun invoke(id: String, title: String?, description: String?, promoted: Boolean?) {
        if(getCurrentUser().hasRole(UserRoles.UPDATE_VIDEOS).not()) throw OperationForbiddenException()

        val updateTitle = title?.let { VideoUpdateCommand.ReplaceTitle(VideoId(id), it) }
        val updateDescription = description?.let { VideoUpdateCommand.ReplaceDescription(VideoId(id), it) }
        val replacePromoted = promoted?.let { VideoUpdateCommand.ReplacePromoted(VideoId(id), it) }

        videoRepository.bulkUpdate(listOfNotNull(updateTitle, updateDescription, replacePromoted))
        logger.info { "Updated video $id" }
    }
}
