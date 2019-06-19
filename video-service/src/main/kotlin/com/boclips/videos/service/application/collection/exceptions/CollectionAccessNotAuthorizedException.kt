package com.boclips.videos.service.application.collection.exceptions

import com.boclips.videos.service.domain.model.collection.UserId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CollectionAccessNotAuthorizedException(userId: UserId, collectionId: String) :
    Exception("user='${userId.value}' collection='$collectionId'")