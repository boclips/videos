package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContentCategoriesLinkBuilderTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentCategoriesLinkBuilder: ContentCategoriesLinkBuilder

    @Test
    fun `returns a link to available video types`() {
        setSecurityContext("api@boclips.com", UserRoles.VIEW_CONTENT_CATEGORIES)

        val link = contentCategoriesLinkBuilder.contentCategries()

        Assertions.assertThat(link?.rel?.value()).isEqualTo("contentCategories")
        Assertions.assertThat(link?.href).endsWith("/v1/content-categories")
    }
}

