package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.presentation.subject.SubjectResource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

class SubjectsLinkBuilderTest {
    private lateinit var subjectsLinkBuilder: SubjectsLinkBuilder

    @BeforeEach
    internal fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        subjectsLinkBuilder = SubjectsLinkBuilder(mock)
    }

    @Test
    fun subjects() {
        assertThat(subjectsLinkBuilder.subjects()).isEqualTo(Link("https://localhost/v1/subjects", "subjects"))
    }

    @Test
    fun `subjects link with rel`() {
        assertThat(subjectsLinkBuilder.subjects("rel")).isEqualTo(Link("https://localhost/v1/subjects", "rel"))
    }

    @Test
    fun `subject link defaults to self`() {
        assertThat(subjectsLinkBuilder.subject(SubjectResource("id"))).isEqualTo(Link("https://localhost/v1/subjects/id", "self"))
    }

    @Test
    fun `subject link with rel`() {
        assertThat(subjectsLinkBuilder.subject(SubjectResource("id"), "rel")).isEqualTo(Link("https://localhost/v1/subjects/id", "rel"))
    }
}