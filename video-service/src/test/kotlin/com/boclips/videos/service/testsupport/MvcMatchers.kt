package com.boclips.videos.service.testsupport

import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import java.util.concurrent.TimeUnit

object MvcMatchers {
    fun cacheableFor(maxAge: Long, unit: TimeUnit) = CacheableFor(unit.toSeconds(maxAge))
    fun halJson() = HalJson()
}

class HalJson : ResultMatcher {
    override fun match(result: MvcResult) {
        assertThat(result.response.contentType).matches("^application/hal\\+json.*$")
    }
}

class CacheableFor(private val seconds: Long) : ResultMatcher {
    override fun match(result: MvcResult) {
        assertThat(result.response.getHeaderValue("Cache-Control"))
            .isEqualTo("max-age=${seconds}, public")
    }
}
