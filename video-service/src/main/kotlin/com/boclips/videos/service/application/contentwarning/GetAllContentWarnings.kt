package com.boclips.videos.service.application.contentwarning

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.service.ContentWarningRepository

class GetAllContentWarnings(
    private val contentWarningRepository: ContentWarningRepository
) {
    operator fun invoke(): List<ContentWarning> = contentWarningRepository.findAll()
}
