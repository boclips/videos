package com.boclips.api.presentation.resources

import org.springframework.hateoas.ResourceSupport
import org.springframework.web.util.UriComponentsBuilder

class Package(
        val name: String,
        val excludedContentProviders: List<ContentProvider> = emptyList()
) : ResourceSupport() {

    companion object {
        fun fromPackage(p: com.boclips.api.domain.model.Package, uriBuilder: UriComponentsBuilder): Package {
            val packageResource = Package(
                    name = p.name,
                    excludedContentProviders = p.excludedContentProviders.map { ContentProvider.fromContentProvider(it, uriBuilder) }
            )
            packageResource.add(WebfluxLinkBuilder.fromContextPath(uriBuilder).slash("/packages").slash(p.id).withSelfRel())
            packageResource.add(WebfluxLinkBuilder.fromContextPath(uriBuilder)
                    .slash("packages")
                    .slash(p.id)
                    .slash("content-providers?exclude=true")
                    .withRel("excludedContentProvider"))
            return packageResource
        }
    }
}
