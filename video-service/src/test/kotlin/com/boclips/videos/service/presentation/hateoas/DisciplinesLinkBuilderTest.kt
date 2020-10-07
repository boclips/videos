package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
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
            Link.of(
                "https://localhost/v1/disciplines",
                "disciplines"
            )
        )
    }

    @Test
    fun `disciplines link with rel`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)
        assertThat(disciplinesLinkBuilder.disciplines("rel")).isEqualTo(Link.of("https://localhost/v1/disciplines", "rel"))
    }

    @Test
    fun `discipline link defaults to self`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)

        assertThat(disciplinesLinkBuilder.discipline(id = "id")).isEqualTo(
            Link.of("https://localhost/v1/disciplines/id", "self")
        )
    }

    @Test
    fun `discipline link with rel`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISCIPLINES)
        assertThat(
            disciplinesLinkBuilder.discipline(
                "rel",
                "id"
            )
        ).isEqualTo(Link.of("https://localhost/v1/disciplines/id", "rel"))
    }

    @Test
    fun `subjects for discipline`() {
        setSecurityContext("teacher@boclips.com", UserRoles.UPDATE_DISCIPLINES)

        assertThat(disciplinesLinkBuilder.subjectsForDiscipline("id")).isEqualTo(
            Link.of("https://localhost/v1/disciplines/id/subjects", "subjects")
        )
    }

    @Test
    fun `update discipline link`() {
        setSecurityContext("admin@boclips.com", UserRoles.UPDATE_DISCIPLINES)

        assertThat(disciplinesLinkBuilder.updateDiscipline("id")).isEqualTo(
            Link.of("https://localhost/v1/disciplines/id", "update")
        )
    }
}
