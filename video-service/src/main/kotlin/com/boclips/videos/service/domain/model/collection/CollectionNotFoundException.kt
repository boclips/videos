package com.boclips.videos.service.domain.model.collection

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CollectionNotFoundException(id: String) : RuntimeException(id)