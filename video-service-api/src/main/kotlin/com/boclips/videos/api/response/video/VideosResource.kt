package com.boclips.videos.api.response.video

import com.boclips.videos.api.response.ResourceWithLinks
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedResources

class VideosResource(
    var _embedded: VideosWrapperResource,
    var page: PagedResources.PageMetadata? = null,
    override var _links: Map<String, Link>?
) : ResourceWithLinks()

data class VideosWrapperResource(
    val videos: List<VideoResource>
)