package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.infrastructure.video.ContentWarningDocument
import org.bson.types.ObjectId

object ContentWarningDocumentConverter {
    fun toContentWarning(document: ContentWarningDocument): ContentWarning =
        ContentWarning(id = ContentWarningId(document.id.toString()), label = document.label)

    fun toDocument(contentWarning: ContentWarning): ContentWarningDocument =
        ContentWarningDocument(id = ObjectId(contentWarning.id.value), label = contentWarning.label)
}
