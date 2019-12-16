package com.boclips.videos.service.application.collection.exceptions

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.UserId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CollectionAccessNotAuthorizedException(userId: UserId, collectionId: String) :
    RuntimeException("user='${userId.value}' collection='$collectionId'") {

    constructor(userId: UserId, collectionId: CollectionId) : this(userId, collectionId.value)
}
