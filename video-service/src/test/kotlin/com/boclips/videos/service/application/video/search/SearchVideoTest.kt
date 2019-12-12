package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.search.SearchVideo.Companion.isAlias
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SearchVideoTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchVideo: SearchVideo

    @Test
    fun `can distinguish aliases from hexadecimal MongoDB ids`() {
        assertThat(isAlias("1232352345")).isTrue()

        assertThat(isAlias(TestFactories.aValidId())).isFalse()
        assertThat(isAlias("random")).isFalse()
        assertThat(isAlias("")).isFalse()
    }

    @Test
    fun `throws exception when id is null`() {
        Assertions.assertThatThrownBy { searchVideo.byId(null, UserFactory.sample()) }
            .isInstanceOf(SearchRequestValidationException::class.java)
    }
}
