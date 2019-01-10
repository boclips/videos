package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.video.exceptions.InvalidCreateVideoRequestException
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateVideoRequestToAssetConverterTest {

    lateinit var converter: CreateVideoRequestToAssetConverter

    @BeforeEach
    fun setUp() {
        converter = CreateVideoRequestToAssetConverter()
    }

    @Test
    fun `throws when playback provider is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(playbackProvider = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("playback provider cannot be null")
    }

    @Test
    fun `throws when playback id is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(playbackId = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("playback id cannot be null")
    }

    @Test
    fun `throws when title is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(title = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("title cannot be null")
    }

    @Test
    fun `throws when description is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(description = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("description cannot be null")
    }

    @Test
    fun `throws when duration is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(duration = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("duration cannot be null")
    }

    @Test
    fun `throws when keywords is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(keywords = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("keywords cannot be null")
    }

    @Test
    fun `throws when releasedOn is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(releasedOn = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("releasedOn cannot be null")
    }

    @Test
    fun `throws when contentProvider is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(provider = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("contentPartnerId cannot be null")
    }

    @Test
    fun `throws when contentProviderId is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(providerVideoId = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("contentPartnerVideoId cannot be null")
    }

    @Test
    fun `throws when content type is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(contentType = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("content type cannot be null")
    }

    @Test
    fun `empty string when restrictions is null`() {
        assertThat(converter.convert(TestFactories.createCreateVideoRequest(legalRestrictions = null)).legalRestrictions).isEmpty()
    }

    @Test
    fun `throws when subjects is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(subjects = null)) }
                .isInstanceOf(InvalidCreateVideoRequestException::class.java)
                .hasMessage("subjects cannot be null")
    }
}