package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.SearchableVideoMetadata

object SearchableVideoMetadataFactory {
    fun create(
            id: String,
            title: String = "",
            description: String = "",
            referenceId: String = ""
    ) = SearchableVideoMetadata(
            id = id,
            title = title,
            description = description,
            referenceId = referenceId
    )
}
