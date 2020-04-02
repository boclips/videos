package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.VideoServiceApiFactory.Companion.createCreateVideoRequest
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createKalturaPlayback
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

class CreateVideoRequestToVideoConverterTest {

    lateinit var converter: CreateVideoRequestToVideoConverter
    lateinit var videoPlayback: VideoPlayback
    lateinit var contentPartner: ContentPartner
    lateinit var subjects: List<Subject>

    @BeforeEach
    fun setUp() {
        converter =
            CreateVideoRequestToVideoConverter()
        videoPlayback = createKalturaPlayback()
        contentPartner = TestFactories.createContentPartner()
        subjects = listOf(TestFactories.createSubject())
    }

    @Test
    fun `sets ingestion timestamp`() {
        val video = converter.convert(createCreateVideoRequest(), createKalturaPlayback(), contentPartner, subjects)

        assertThat(video.ingestedAt).isAfter(LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC))
        assertThat(video.ingestedAt).isBefore(LocalDate.now().plusDays(2).atStartOfDay(ZoneOffset.UTC))
    }

    @Test
    fun `uses the playback duration`() {
        val expectedDuration = Duration.ofMinutes(1)
        val playback = createKalturaPlayback(duration = expectedDuration)
        val video = converter.convert(createCreateVideoRequest(), playback, contentPartner, subjects)

        assertThat(video.playback.duration).isEqualTo(expectedDuration)
    }

    @Test
    fun `uses the playback`() {
        val playback = createKalturaPlayback()
        val video = converter.convert(createCreateVideoRequest(), playback, contentPartner, subjects)

        assertThat(video.playback).isEqualTo(playback)
    }

    @Test
    fun `uses the subjects and sets subjectsWereSetManually to true`() {
        val playback = createKalturaPlayback()

        val video = converter.convert(
            createCreateVideoRequest(),
            playback,
            contentPartner,
            subjects
        )

        assertThat(video.subjects.items).hasSize(1)
        assertThat(video.subjects.items.first().id).isNotNull
        assertThat(video.subjects.setManually).isTrue()
    }

    @Test
    fun `without subjects, subjectsWereSetManually is false`() {
        val playback = createKalturaPlayback()

        val video = converter.convert(
            createCreateVideoRequest(),
            playback,
            contentPartner,
            listOf()
        )

        assertThat(video.subjects.setManually).isFalse()
    }

    @Test
    fun `empty string when restrictions is null`() {
        assertThat(
            converter.convert(
                createCreateVideoRequest(legalRestrictions = null),
                videoPlayback,
                contentPartner,
                subjects
            ).legalRestrictions
        ).isEmpty()
    }
}
