package com.boclips.videos.service.presentation.resources

import org.springframework.hateoas.ResourceSupport
import org.springframework.web.util.UriComponentsBuilder

data class ContentProvider(val name: String) : ResourceSupport() {
    companion object {
        fun fromContentProvider(p: com.boclips.videos.service.domain.model.ContentProvider, uriBuilder: UriComponentsBuilder): ContentProvider {
            val contentProvider = ContentProvider(
                    name = p.name
            )
            contentProvider.add(WebfluxLinkBuilder.fromContextPath(uriBuilder).slash("/content-providers").slash(p.id).withSelfRel())
            return contentProvider
        }
    }
}
