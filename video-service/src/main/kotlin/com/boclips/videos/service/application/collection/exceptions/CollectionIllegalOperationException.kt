package com.boclips.videos.service.application.collection.exceptions

import com.boclips.videos.service.domain.model.common.UserId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class CollectionIllegalOperationException(userId: UserId, collectionId: String, operation: String) :
    Exception("user='${userId.value}' collection='$collectionId' Cannot $operation")