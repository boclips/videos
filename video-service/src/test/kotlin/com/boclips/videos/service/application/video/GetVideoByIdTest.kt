package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
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

class GetVideoByIdIsAliasTest {
    @Test
    fun `can distinguish aliases from hexadecimal MongoDB ids`() {
        assertThat(GetVideoById.isAlias("1232352345")).isTrue()

        assertThat(GetVideoById.isAlias(TestFactories.aValidId())).isFalse()
        assertThat(GetVideoById.isAlias("random")).isFalse()
        assertThat(GetVideoById.isAlias("")).isFalse()
    }
}