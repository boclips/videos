package com.boclips.videos.service.application

import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.converters.AttachmentToResourceConverter
import org.bson.types.ObjectId

class AddAttachment(private val videoRepository: VideoRepository) {
    fun toVideo(user: User, attachment: AttachmentRequest, videoId: VideoId): Attachment {
        if (user.isPermittedToUpdateVideo.not()) throw OperationForbiddenException()

        val savedAttachment = Attachment(
            attachmentId = AttachmentId(value = ObjectId().toHexString()),
            description = attachment.description!!,
            linkToResource = attachment.linkToResource,
            type = AttachmentType.valueOf(attachment.type)
        )

        videoRepository.update(VideoUpdateCommand.AddAttachment(videoId = videoId, attachment = savedAttachment))

        return savedAttachment
    }
}