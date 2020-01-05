package com.boclips.videos.api.response.subject

import com.boclips.videos.api.response.ResourceWithLinks
import org.springframework.hateoas.Link

data class SubjectResource(
    val id: String,
    val name: String? = null,
    val lessonPlan: Boolean? = false,
    override var _links: Map<String, Link>? = null
) : ResourceWithLinks()