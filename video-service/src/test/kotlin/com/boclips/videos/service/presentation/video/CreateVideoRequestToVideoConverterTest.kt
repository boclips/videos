package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.web.exceptions.BoclipsApiException
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Condition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.function.Predicate

class CreateVideoRequestToVideoConverterTest {

    lateinit var converter: CreateVideoRequestToVideoConverter
    lateinit var videoPlayback: VideoPlayback

    @BeforeEach
    fun setUp() {
        converter = CreateVideoRequestToVideoConverter()
        videoPlayback = TestFactories.createKalturaPlayback()
    }

    @Test
    fun `uses the playback duration`() {
        val expectedDuration = Duration.ofMinutes(1)
        val playback = TestFactories.createKalturaPlayback(duration = expectedDuration)
        val video = converter.convert(TestFactories.createCreateVideoRequest(), playback)

        assertThat(video.playback.duration).isEqualTo(expectedDuration)
    }

    @Test
    fun `uses the playback`() {
        val playback = TestFactories.createKalturaPlayback()
        val video = converter.convert(TestFactories.createCreateVideoRequest(), playback)

        assertThat(video.playback).isEqualTo(playback)
    }

    @Test
    fun `throws when title is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(title = null), videoPlayback) }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("title cannot be null")
    }

    @Test
    fun `throws when description is null`() {
        assertThatThrownBy {
            converter.convert(
                TestFactories.createCreateVideoRequest(description = null),
                videoPlayback
            )
        }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("description cannot be null")
    }

    @Test
    fun `throws when keywords is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(keywords = null), videoPlayback) }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("keywords cannot be null")
    }

    @Test
    fun `throws when releasedOn is null`() {
        assertThatThrownBy {
            converter.convert(
                TestFactories.createCreateVideoRequest(releasedOn = null),
                videoPlayback
            )
        }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("releasedOn cannot be null")
    }

    @Test
    fun `throws when contentProvider is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(provider = null), videoPlayback) }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("contentPartnerId cannot be null")
    }

    @Test
    fun `throws when contentProviderId is null`() {
        assertThatThrownBy {
            converter.convert(
                TestFactories.createCreateVideoRequest(providerVideoId = null),
                videoPlayback
            )
        }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("contentPartnerVideoId cannot be null")
    }

    @Test
    fun `throws when content type is null`() {
        assertThatThrownBy {
            converter.convert(
                TestFactories.createCreateVideoRequest(contentType = null),
                videoPlayback
            )
        }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("content type cannot be null")
    }

    @Test
    fun `empty string when restrictions is null`() {
        assertThat(
            converter.convert(
                TestFactories.createCreateVideoRequest(legalRestrictions = null),
                videoPlayback
            ).legalRestrictions
        ).isEmpty()
    }

    @Test
    fun `throws when subjects is null`() {
        assertThatThrownBy { converter.convert(TestFactories.createCreateVideoRequest(subjects = null), videoPlayback) }
            .isInstanceOf(NonNullableFieldCreateRequestException::class.java)
            .hasBoclipsApiErrorMessage("subjects cannot be null")
    }
}

private fun AbstractThrowableAssert<*,*>.hasBoclipsApiErrorMessage(s: String) {
    this.has(Condition(Predicate { t -> (t as BoclipsApiException).exceptionDetails.message == s }, "cannot find exception message"))
}
