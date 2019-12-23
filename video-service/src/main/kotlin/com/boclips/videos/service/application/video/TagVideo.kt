package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.TagVideoRequest
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import com.boclips.web.exceptions.ResourceNotFoundApiException
import mu.KLogging
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
class TagVideo(
    private val videoRepository: VideoRepository,
    private val tagRepository: TagRepository
) {

    companion object : KLogging();

    operator fun invoke(@Valid tagVideoRequest: TagVideoRequest, user: User) {
        val tag = try {
            tagRepository.findById(TagId(tagVideoRequest.tagUrl!!.substringAfterLast("/")))
        } catch (e: Exception) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    "Invalid tag URL",
                    "The tag URL is malformed or cannot be processed"
                )
            )
        } ?: throw ResourceNotFoundApiException("Invalid tag URL", "The tag URL cannot be found")

        videoRepository.update(
            VideoUpdateCommand.ReplaceTag(
                VideoId(tagVideoRequest.videoId!!),
                UserTag(tag = tag, userId = user.id)
            )
        )
    }
}
