package com.boclips.videos.service.testsupport

import org.assertj.core.api.Assertions
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import java.util.concurrent.TimeUnit

object MvcMatchers {
    fun cacheableFor(maxAge: Long, unit: TimeUnit) = CacheableFor(unit.toSeconds(maxAge))
}

class CacheableFor(private val seconds: Long) : ResultMatcher {
    override fun match(result: MvcResult) {
        Assertions.assertThat(result.response.getHeaderValue("Cache-Control"))
            .isEqualTo("max-age=${seconds}, public")
        Assertions.assertThat(result.response.getHeaderValue("Content-Length"))
            .isEqualTo(result.response.contentAsString.length)
    }
}
