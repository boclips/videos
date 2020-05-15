package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.infrastructure.video.ContentWarningDocument

object ContentWarningDocumentConverter {
    fun toContentWarning(document: ContentWarningDocument): ContentWarning {
        return ContentWarning(id = ContentWarningId(document.id.toString()), label = document.label)
    }
}