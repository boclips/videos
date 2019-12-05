package com.boclips.videos.service.presentation.hateoas

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.presentation.subject.SubjectResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoTypeRelProviderTest {
    val provider = VideoTypeRelProvider()

    @Test
    fun `returns "videoType" for single resource`() {
        assertThat(provider.getItemResourceRelFor(VideoType::class.java)).isEqualTo("videoType")
    }

    @Test
    fun `returns "videoTypes" for a collection of resources`() {
        assertThat(provider.getCollectionResourceRelFor(VideoType::class.java)).isEqualTo("videoTypes")
    }

    @Test
    fun `handles VideoType class`() {
        assertThat(provider.supports(VideoType::class.java)).isTrue()
    }

    @Test
    fun `does not support other types`() {
        assertThat(provider.supports(SubjectResource::class.java)).isFalse()
    }
}