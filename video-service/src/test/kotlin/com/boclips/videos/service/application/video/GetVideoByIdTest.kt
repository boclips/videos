package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class GetVideoByIdTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getVideoById: GetVideoById

    @Test
    fun `throws exception when query is null`() {
        assertThatThrownBy { getVideoById.execute(null) }.isInstanceOf(QueryValidationException::class.java)
    }
}