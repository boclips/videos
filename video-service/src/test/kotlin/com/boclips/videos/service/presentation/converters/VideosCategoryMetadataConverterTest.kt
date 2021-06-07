package com.boclips.videos.service.presentation.converters

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideosCategoryMetadataConverterTest : AbstractSpringIntegrationTest() {

    @Test
    fun `groups categories by video id`() {

        val videoId1 = TestFactories.createVideoId()
        val videoId2 = TestFactories.createVideoId()
        val videoId3 = TestFactories.createVideoId()

        val result = VideosCategoryMetadataConverter.convert(
            listOf(
                CategoryMappingMetadata(videoId1.value, "1a"),
                CategoryMappingMetadata(videoId1.value, "1b"),
                CategoryMappingMetadata(videoId2.value, "2a"),
                CategoryMappingMetadata(videoId3.value, "3a"),
            )
        )

        assertThat(result[videoId1]).containsExactly("1a", "1b")
        assertThat(result[videoId2]).containsExactly("2a")
        assertThat(result[videoId3]).containsExactly("3a")
    }

    @Test
    fun `ignores entries with empty categories`() {
        val videoId1 = TestFactories.createVideoId()

        val result = VideosCategoryMetadataConverter.convert(
            listOf(
                CategoryMappingMetadata(videoId1.value, "1a"),
                CategoryMappingMetadata(videoId1.value, ""),
            )
        )

        assertThat(result[videoId1]).containsExactly("1a")
    }
}
