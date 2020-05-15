package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.contentwarning.ContentWarningResource
import com.boclips.videos.api.response.contentwarning.ContentWarningWrapper
import com.boclips.videos.api.response.contentwarning.ContentWarningsResource
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning

class ContentWarningToResourceConverter {

    fun convert(contentWarning: ContentWarning): ContentWarningResource {
        return ContentWarningResource(id = contentWarning.id.value, label = contentWarning.label)
    }

    fun convert(contentWarnings: List<ContentWarning>): ContentWarningsResource {
        return ContentWarningsResource(
            _embedded = ContentWarningWrapper(
                contentWarnings = contentWarnings.map { convert(it) }
            ),
            _links = emptyMap()
        )
    }
}