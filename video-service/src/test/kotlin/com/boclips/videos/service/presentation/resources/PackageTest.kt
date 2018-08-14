package com.boclips.videos.service.presentation.resources

import com.boclips.videos.service.domain.model.ContentProvider
import com.boclips.videos.service.domain.model.Package
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

class PackageTest {
    @Test
    fun fromPackage_transformsName() {
        val packageResource = com.boclips.videos.service.presentation.resources.Package.fromPackage(Package("package-id", "name"), UriComponentsBuilder.newInstance())

        assertThat(packageResource.name).isEqualTo("name")
    }

    @Test
    fun fromPackage_createsSelfLinks() {
        val packageResource = com.boclips.videos.service.presentation.resources.Package.fromPackage(Package("package-id", "name"), UriComponentsBuilder.newInstance())

        assertThat(packageResource.getLink("self")).isEqualTo(Link("/packages/package-id"))
    }

    @Test
    fun fromPackage_createsExcludedContentProviderLink() {
        val packageResource = com.boclips.videos.service.presentation.resources.Package.fromPackage(Package("package-id", "name"), UriComponentsBuilder.newInstance())

        assertThat(packageResource.getLink("excludedContentProvider")).isEqualTo(Link("/packages/package-id/content-providers?exclude=true", "excludedContentProvider"))
    }

    @Test
    fun fromPackage_transformsExcludedContentProviders() {
        val packageResource = com.boclips.videos.service.presentation.resources.Package.fromPackage(Package("package-id", "name", listOf(ContentProvider(name = "item-1"), ContentProvider(name = "item-2"))), UriComponentsBuilder.newInstance())

        assertThat(packageResource.excludedContentProviders).hasSize(2)
        assertThat(packageResource.excludedContentProviders.first().name).isEqualTo("item-1")
    }

}