package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.VideoMetadata

object SearchableVideoMetadataFactory {
    fun create(
            id: String,
            title: String = "",
            description: String = ""
    ) = VideoMetadata(
            id = id,
            title = title,
            description = description
    )
}
