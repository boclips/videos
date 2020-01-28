package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class SubjectsLinkBuilderTest {

    lateinit var subjectsLinkBuilder: SubjectsLinkBuilder

    @BeforeEach
    internal fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        subjectsLinkBuilder = SubjectsLinkBuilder(mock)
    }

    @Test
    fun subjects() {
        assertThat(subjectsLinkBuilder.subjects()).isEqualTo(HateoasLink("https://localhost/v1/subjects", "subjects"))
    }

    @Test
    fun `subjects link with rel`() {
        assertThat(subjectsLinkBuilder.subjects("rel")).isEqualTo(HateoasLink("https://localhost/v1/subjects", "rel"))
    }

    @Test
    fun `subject link defaults to self`() {
        val id = SubjectResource("id")
        assertThat(subjectsLinkBuilder.self(id.id)).isEqualTo(
            HateoasLink(
                "https://localhost/v1/subjects/id",
                "self"
            )
        )
    }

    @Test
    fun `update subject link present for users with UPDATE_SUBJECTS role`() {
        setSecurityContext("employee@boclips.com", UserRoles.UPDATE_SUBJECTS)

        assertThat(subjectsLinkBuilder.updateSubject(SubjectResource("id"))).isEqualTo(
            HateoasLink(
                "https://localhost/v1/subjects/id",
                "update"
            )
        )
    }

    @Test
    fun `update subject link does not present for users without UPDATE_SUBJECTS role`() {
        assertThat(subjectsLinkBuilder.updateSubject(SubjectResource("id"))).isNull()
    }

    @Test
    fun `subject link with self`() {
        val id = SubjectResource("id")
        assertThat(
            subjectsLinkBuilder.self(
                id.id
            )
        ).isEqualTo(HateoasLink("https://localhost/v1/subjects/id", "self"))
    }
}
