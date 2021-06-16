package com.boclips.videos.service.application.video

import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specifiable
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import org.bson.types.ObjectId

class AttachmentRequestConverter {
    fun convert(videoId: VideoId, newAttachments: Specifiable<List<AttachmentRequest>>?): VideoUpdateCommand? {
        return newAttachments.let {
            when (it) {
                is Specified -> {
                    val attachments = it.value.map { attachment ->
                        Attachment(
                            attachmentId = AttachmentId(value = ObjectId().toHexString()),
                            description = attachment.description!!,
                            linkToResource = attachment.linkToResource,
                            type = AttachmentType.valueOf(attachment.type)
                        )
                    }
                    VideoUpdateCommand.ReplaceAttachments(videoId = videoId, attachments = attachments)
                }
                is ExplicitlyNull -> VideoUpdateCommand.RemoveAttachments(videoId = videoId)
                null -> null
            }
        }
    }
}
