package com.boclips.videos.service.application.collection.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidAttachmentTypeException(attachmentType: String?) :
    Exception("Attachment type $attachmentType is not valid")
