package com.boclips.api.presentation.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

class ContentProviderTest {
    @Test
    fun fromContentProvider_transformsName() {
        val contentProviderResource = ContentProvider.fromContentProvider(com.boclips.api.domain.model.ContentProvider(name = "name", id = "content-provider-id"), UriComponentsBuilder.newInstance())

        assertThat(contentProviderResource.name).isEqualTo("name")
    }

    @Test
    fun fromContentProvider_createsSelfLinks() {
        val contentProviderResource = ContentProvider.fromContentProvider(com.boclips.api.domain.model.ContentProvider(name = "name", id = "content-provider-id"), UriComponentsBuilder.newInstance())

        assertThat(contentProviderResource.getLink("self")).isEqualTo(Link("/content-providers/content-provider-id"))
    }
}