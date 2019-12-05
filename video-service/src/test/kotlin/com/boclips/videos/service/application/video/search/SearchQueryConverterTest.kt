package com.boclips.videos.service.application.video.search

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.application.video.exceptions.InvalidTypeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SearchQueryConverterTest {
    @Test
    fun `throws an exception when string does not match the video type`() {
        val queryConverter = SearchQueryConverter()
        assertThrows<InvalidTypeException> {
            queryConverter.convertType("doesn't match")
        }
    }

    @Test
    fun `can convert valid video type values`() {
        val queryConverter = SearchQueryConverter()
        VideoType.values().forEach {
            assertThat(queryConverter.convertType(it.name)).isEqualTo(it)
        }
    }
}