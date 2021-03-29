package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Link

class TaxonomyLinkBuilderTest {
    private lateinit var taxonomyLinkBuilder: TaxonomyLinkBuilder
    @BeforeEach
    fun setUp() {
        taxonomyLinkBuilder = TaxonomyLinkBuilder()
    }

    @Test
    fun `get taxonomies link when permitted`() {
        setSecurityContext("evergreen", UserRoles.VIEW_TAXONOMIES)
        Assertions.assertThat(taxonomyLinkBuilder.taxonomies()?.rel?.value()).isEqualTo("taxonomies")
        Assertions.assertThat(taxonomyLinkBuilder.taxonomies()?.href).isEqualTo("/v1/taxonomies")
    }

    @Test
    fun `when no view taxonomies role`() {
        setSecurityContext("bad-user")
        Assertions.assertThat(taxonomyLinkBuilder.taxonomies()).isNull()
    }
}
