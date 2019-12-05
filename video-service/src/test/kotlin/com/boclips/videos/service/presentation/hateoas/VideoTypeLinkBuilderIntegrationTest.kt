package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoTypeLinkBuilderIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoTypeLinkBuilder: VideoTypeLinkBuilder

    @Test
    fun `returns a link to available video types`() {
        setSecurityContext("api@boclips.com", UserRoles.VIEW_VIDEO_TYPES)

        val link = videoTypeLinkBuilder.videoTypes()

        assertThat(link?.rel).isEqualTo("videoTypes")
        assertThat(link?.href).endsWith("/v1/video-types")
    }
}