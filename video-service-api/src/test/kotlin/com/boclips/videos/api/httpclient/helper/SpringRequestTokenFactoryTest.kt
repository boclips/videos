package com.boclips.videos.api.httpclient.helper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class SpringRequestTokenFactoryTest {
    @Test
    fun `obtains a access token from the header`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer blub")
        }

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        val token = SpringRequestTokenFactory().getAccessToken()

        assertThat(token).isEqualTo("blub")
    }

    @Test
    fun `obtains an empty token for an inexistent token`() {
        val request = MockHttpServletRequest()

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        val token = SpringRequestTokenFactory().getAccessToken()

        assertThat(token).isEqualTo("")
    }
}