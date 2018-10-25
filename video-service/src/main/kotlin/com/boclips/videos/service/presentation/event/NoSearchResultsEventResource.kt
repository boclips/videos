package com.boclips.videos.service.presentation.event

import org.springframework.hateoas.core.Relation
import java.time.ZonedDateTime

@Relation(collectionRelation = "events")
class NoSearchResultsEventResource(
        var name: String?,
        var email: String?,
        var query: String?,
        var description: String?,
        var createdAt: ZonedDateTime
)
