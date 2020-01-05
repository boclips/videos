package com.boclips.videos.api.response.subject

import com.boclips.videos.api.response.ResourceWithLinks
import org.springframework.hateoas.Link

class SubjectsResource(
    var _embedded: SubjectsWrapperResource,
    override var _links: Map<String, Link>?
) : ResourceWithLinks()

data class SubjectsWrapperResource(
    val subjects: List<SubjectResource>
)
