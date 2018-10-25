package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent

fun convertToResource(event: NoSearchResultsEvent): NoSearchResultsEventResource {
    return NoSearchResultsEventResource(
            name = event.data.name,
            email = event.data.email,
            query = event.data.query,
            description = event.data.description,
            createdAt = event.timestamp
    )
}