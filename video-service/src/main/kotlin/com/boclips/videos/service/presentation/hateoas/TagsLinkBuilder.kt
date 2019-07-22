package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.presentation.tag.TagResource
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class TagsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun tags(rel: String = "tags"): Link {
        return Link(getTagRoot().toUriString(), rel)
    }

    fun tag(tag: TagResource, rel: String = "self"): Link {
        return Link(getTagRoot().pathSegment(tag.id).toUriString(), rel)
    }

    private fun getTagRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/tags")
        .replaceQueryParams(null)
}