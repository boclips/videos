package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.disciplines.DisciplineResource
import com.boclips.videos.service.testsupport.DisciplineFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

class DisciplinesLinkBuilderTest {
    private lateinit var disciplinesLinkBuilder: DisciplinesLinkBuilder

    @BeforeEach
    internal fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        disciplinesLinkBuilder = DisciplinesLinkBuilder(mock)
    }

    @Test
    fun disciplines() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)
        assertThat(disciplinesLinkBuilder.disciplines()).isEqualTo(
            Link(
                "https://localhost/v1/disciplines",
                "disciplines"
            )
        )
    }

    @Test
    fun `disciplines link with rel`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)
        assertThat(disciplinesLinkBuilder.disciplines("rel")).isEqualTo(Link("https://localhost/v1/disciplines", "rel"))
    }

    @Test
    fun `discipline link defaults to self`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)
        assertThat(disciplinesLinkBuilder.discipline(DisciplineResource.from(DisciplineFactory.sample(id = "id")))).isEqualTo(
            Link("https://localhost/v1/disciplines/id", "self")
        )
    }

    @Test
    fun `discipline link with rel`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)
        assertThat(
            disciplinesLinkBuilder.discipline(
                DisciplineResource.from(DisciplineFactory.sample(id = "id")),
                "rel"
            )
        ).isEqualTo(Link("https://localhost/v1/disciplines/id", "rel"))
    }

    @Test
    fun `subjects for discipline`() {
        setSecurityContext("teacher@boclips.com", UserRoles.UPDATE_DISCIPLINES)

        assertThat(disciplinesLinkBuilder.subjectsForDiscipline(DisciplineResource.from(DisciplineFactory.sample(id = "id")))).isEqualTo(
            Link("https://localhost/v1/disciplines/id/subjects", "subjects")
        )
    }
}
