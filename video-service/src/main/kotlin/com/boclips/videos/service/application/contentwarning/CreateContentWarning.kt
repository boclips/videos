package com.boclips.videos.service.application.contentwarning

import com.boclips.videos.api.request.contentwarning.CreateContentWarningRequest
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.service.ContentWarningRepository
import javax.validation.Valid

class CreateContentWarning(
    private val contentWarningRepository: ContentWarningRepository
) {
    operator fun invoke(@Valid request: CreateContentWarningRequest): ContentWarning =
        contentWarningRepository.create(request.label!!)
}
