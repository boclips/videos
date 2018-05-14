package com.boclips.api.presentation.resources

import com.boclips.api.infrastructure.configuration.WebfluxLinkBuilder
import org.springframework.hateoas.ResourceSupport
import org.springframework.web.util.UriComponentsBuilder

class Package(
        val name: String,
        val excludedContentProviders: List<String> = emptyList()
) : ResourceSupport() {

    companion object {
        fun fromPackage(p: com.boclips.api.Package, uriBuilder: UriComponentsBuilder): Package {
            val packageResource = Package(
                    name = p.name,
                    excludedContentProviders = p.excludedContentProviders
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
