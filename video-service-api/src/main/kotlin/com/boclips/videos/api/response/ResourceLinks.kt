package com.boclips.videos.api.response

import org.springframework.hateoas.Link

abstract class ResourceLinks {
    abstract var _links: Map<String, Link>?
}