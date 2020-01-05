package com.boclips.videos.api.response.subject

import com.boclips.videos.api.response.ResourceLinks
import org.springframework.hateoas.Link

data class SubjectCollectionResource(
    val subjects: List<SubjectResource>
)

data class SubjectResource(
    val id: String,
    val name: String? = null,
    val lessonPlan: Boolean? = false,
    override var _links: Map<String, Link>? = null
) : ResourceLinks()