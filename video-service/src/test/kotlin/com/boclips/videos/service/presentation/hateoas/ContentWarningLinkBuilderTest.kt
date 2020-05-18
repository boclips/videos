package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContentWarningLinkBuilderTest {
    lateinit var contentWarningLinkBuilder: ContentWarningLinkBuilder

    @BeforeEach
    fun setUp() {
        contentWarningLinkBuilder = ContentWarningLinkBuilder()
    }

    @Test
    fun createLink() {
        setSecurityContext("team", UserRoles.CREATE_CONTENT_WARNINGS)

        assertThat(contentWarningLinkBuilder.createLink()?.hasRel("create"))
        assertThat(contentWarningLinkBuilder.createLink()?.href).isNotBlank()
    }

    @Test
    fun getAllLink() {
        setSecurityContext("team", UserRoles.VIEW_CONTENT_WARNINGS)

        assertThat(contentWarningLinkBuilder.getAllLink()?.hasRel("contentWarnings"))
        assertThat(contentWarningLinkBuilder.getAllLink()?.href).isNotBlank()
    }

    @Test
    fun self() {
        assertThat(contentWarningLinkBuilder.self("123").hasRel("self"))
        assertThat(contentWarningLinkBuilder.self("123").href).contains("/v1/content-warnings/123")
    }
}