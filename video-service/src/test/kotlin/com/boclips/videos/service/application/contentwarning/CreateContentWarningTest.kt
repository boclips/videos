package com.boclips.videos.service.application.contentwarning

import com.boclips.videos.api.request.contentwarning.CreateContentWarningRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions

import org.junit.jupiter.api.Test

class CreateContentWarningTest() : AbstractSpringIntegrationTest() {

    @Test
    fun create() {
        val warning = createContentWarning(CreateContentWarningRequest(label = "New Warning"))
        Assertions.assertThat(warning.id.value).isNotNull()
        Assertions.assertThat(warning.label).isEqualTo("New Warning")
    }
}