package com.boclips.videos.api.response

import org.springframework.hateoas.Link

abstract class ResourceWithLinks {
    abstract var _links: Map<String, Link>?
}