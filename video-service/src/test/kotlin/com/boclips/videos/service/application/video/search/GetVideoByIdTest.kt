package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class GetVideoByIdTest {

    @Test
    fun `can distinguish aliases from hexadecimal MongoDB ids`() {
        assertThat(GetVideoById.isAlias("1232352345")).isTrue()

        assertThat(GetVideoById.isAlias(TestFactories.aValidId())).isFalse()
        assertThat(GetVideoById.isAlias("random")).isFalse()
        assertThat(GetVideoById.isAlias("")).isFalse()
    }

    @Test
    fun `throws exception when query is null`() {
        assertThatThrownBy { GetVideoById(mock(), mock(), mock()).execute(null) }.isInstanceOf(QueryValidationException::class.java)
    }
}