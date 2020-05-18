package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.contentwarning.ContentWarningResource
import com.boclips.videos.api.response.contentwarning.ContentWarningWrapper
import com.boclips.videos.api.response.contentwarning.ContentWarningsResource
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.presentation.hateoas.ContentWarningLinkBuilder

class ContentWarningToResourceConverter(
    private val linkBuilder: ContentWarningLinkBuilder
) {

    fun convert(contentWarning: ContentWarning): ContentWarningResource {
        return ContentWarningResource(
            id = contentWarning.id.value,
            label = contentWarning.label,
            _links = listOfNotNull(linkBuilder.self(contentWarning.id.value)).map { it.rel.value() to it }.toMap()
        )
    }

    fun convert(contentWarnings: List<ContentWarning>): ContentWarningsResource {
        return ContentWarningsResource(
            _embedded = ContentWarningWrapper(
                contentWarnings = contentWarnings.map { convert(it) }
            ),
            _links = listOfNotNull(linkBuilder.createLink()).map { it.rel.value() to it }.toMap()
        )
    }
}