package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.VideoServiceApiFactory.Companion.createCreateVideoRequest
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.channel.Channel
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
    lateinit var contentPartner: Channel
    lateinit var subjects: List<Subject>
    lateinit var categories: Map<CategorySource, Set<CategoryWithAncestors>>

    @BeforeEach
    fun setUp() {
        converter =
            CreateVideoRequestToVideoConverter()
        videoPlayback = createKalturaPlayback()
        contentPartner = TestFactories.createChannel()
        subjects = listOf(TestFactories.createSubject())
        categories = mapOf(
            CategorySource.MANUAL to setOf(
                CategoryWithAncestors(
                    codeValue = CategoryCode("A"),
                    description = "this is description"
                )
            )
        )
    }

    @Test
    fun `sets ingestion timestamp`() {
        val video = converter.convert(createCreateVideoRequest(), createKalturaPlayback(), contentPartner, subjects, categories)

        assertThat(video.ingestedAt).isAfter(LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC))
        assertThat(video.ingestedAt).isBefore(LocalDate.now().plusDays(2).atStartOfDay(ZoneOffset.UTC))
    }

    @Test
    fun `uses the playback duration`() {
        val expectedDuration = Duration.ofMinutes(1)
        val playback = createKalturaPlayback(duration = expectedDuration)
        val video = converter.convert(createCreateVideoRequest(), playback, contentPartner, subjects, categories)

        assertThat(video.playback.duration).isEqualTo(expectedDuration)
    }

    @Test
    fun `uses the playback`() {
        val playback = createKalturaPlayback()
        val video = converter.convert(createCreateVideoRequest(), playback, contentPartner, subjects, categories)

        assertThat(video.playback).isEqualTo(playback)
    }

    @Test
    fun `uses the subjects and sets subjectsWereSetManually to true`() {
        val playback = createKalturaPlayback()

        val video = converter.convert(
            createCreateVideoRequest(),
            playback,
            contentPartner,
            subjects,
            categories
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
            listOf(),
            categories
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
                subjects,
                categories
            ).legalRestrictions
        ).isEmpty()
    }

    @Test
    fun `sets video to WithVoice when isVoiced is true`() {
        val video = converter.convert(
            createCreateVideoRequest(isVoiced = true),
            videoPlayback,
            contentPartner,
            subjects,
            categories
        )

        assertThat(video.isVoiced()).isTrue()
    }

    @Test
    fun `sets video to WithoutVoice when isVoiced is false`() {
        val video = converter.convert(
            createCreateVideoRequest(isVoiced = false),
            videoPlayback,
            contentPartner,
            subjects,
            categories
        )

        assertThat(video.isVoiced()).isFalse()
    }

    @Test
    fun `sets video to UnknownVoice when isVoiced is null`() {
        val video = converter.convert(
            createCreateVideoRequest(isVoiced = null),
            videoPlayback,
            contentPartner,
            subjects,
            categories
        )

        assertThat(video.isVoiced()).isNull()
    }
}
