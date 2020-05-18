package com.boclips.videos.service.application.contentwarning

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.domain.service.ContentWarningRepository

class GetContentWarning(
    private val contentWarningRepository: ContentWarningRepository
) {
    operator fun invoke(id: String): ContentWarning? = contentWarningRepository.findById(ContentWarningId(id))
}
