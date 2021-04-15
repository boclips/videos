package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TaxonomyLinkBuilderTest {
    private lateinit var taxonomyLinkBuilder: TaxonomyLinkBuilder
    @BeforeEach
    fun setUp() {
        taxonomyLinkBuilder = TaxonomyLinkBuilder()
    }

    @Test
    fun `get categories link when permitted`() {
        setSecurityContext("evergreen", UserRoles.VIEW_TAXONOMIES)
        Assertions.assertThat(taxonomyLinkBuilder.categories()?.rel?.value()).isEqualTo("categories")
        Assertions.assertThat(taxonomyLinkBuilder.categories()?.href).endsWith("/v1/categories")
    }
}
