package com.boclips.videos.api.response.subject

import com.boclips.videos.api.response.ResourceLinks
import org.springframework.hateoas.Link

class SubjectsResource(
    var _embedded: SubjectCollectionResource,
    override var _links: Map<String, Link>?
) : ResourceLinks()

