package com.boclips.videos.presentation.resources

import com.boclips.videos.domain.model.ContentProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

class PackageTest {
    @Test
    fun fromPackage_transformsName() {
        val packageResource = Package.fromPackage(com.boclips.videos.domain.model.Package("package-id", "name"), UriComponentsBuilder.newInstance())

        assertThat(packageResource.name).isEqualTo("name")
    }

    @Test
    fun fromPackage_createsSelfLinks() {
        val packageResource = Package.fromPackage(com.boclips.videos.domain.model.Package("package-id", "name"), UriComponentsBuilder.newInstance())

        assertThat(packageResource.getLink("self")).isEqualTo(Link("/packages/package-id"))
    }

    @Test
    fun fromPackage_createsExcludedContentProviderLink() {
        val packageResource = Package.fromPackage(com.boclips.videos.domain.model.Package("package-id", "name"), UriComponentsBuilder.newInstance())

        assertThat(packageResource.getLink("excludedContentProvider")).isEqualTo(Link("/packages/package-id/content-providers?exclude=true", "excludedContentProvider"))
    }

    @Test
    fun fromPackage_transformsExcludedContentProviders() {
        val packageResource = Package.fromPackage(com.boclips.videos.domain.model.Package("package-id", "name", listOf(ContentProvider(name = "item-1"), ContentProvider(name = "item-2"))), UriComponentsBuilder.newInstance())

        assertThat(packageResource.excludedContentProviders).hasSize(2)
        assertThat(packageResource.excludedContentProviders.first().name).isEqualTo("item-1")
    }

}