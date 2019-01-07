package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.VideoMetadata

object SearchableVideoMetadataFactory {
    fun create(
            id: String,
            title: String = "",
            description: String = "",
            contentProvider: String = "",
            keywords: List<String> = emptyList(),
            typeId: Int = 2
    ) = VideoMetadata(
            id = id,
            title = title,
            description = description,
            contentProvider = contentProvider,
            keywords = keywords,
            typeId = typeId,
            isEducational = true
    )
}
