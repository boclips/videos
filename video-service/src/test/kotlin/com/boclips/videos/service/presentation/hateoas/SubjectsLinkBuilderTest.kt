package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Link

class SubjectsLinkBuilderTest : AbstractSpringIntegrationTest(){

    @Autowired
    lateinit var subjectsLinkBuilder: SubjectsLinkBuilder

    @Test
    fun subjects() {
        assertThat(subjectsLinkBuilder.subjects()).isEqualTo(Link("http://localhost/v1/subjects", "subjects"))
    }

    @Test
    fun `subjects link with rel`() {
        assertThat(subjectsLinkBuilder.subjects("rel")).isEqualTo(Link("http://localhost/v1/subjects", "rel"))
    }

    @Test
    fun `subject link defaults to self`() {
        assertThat(subjectsLinkBuilder.self(SubjectResource("id"))).isEqualTo(
            Link(
                "http://localhost/v1/subjects/id",
                "self"
            )
        )
    }

    @Test
    fun `update subject link present for users with UPDATE_SUBJECTS role`() {
        setSecurityContext("employee@boclips.com", UserRoles.UPDATE_SUBJECTS)

        assertThat(subjectsLinkBuilder.updateSubject(SubjectResource("id"))).isEqualTo(
            Link(
                "http://localhost/v1/subjects/id",
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
        assertThat(
            subjectsLinkBuilder.self(
                SubjectResource("id")
            )
        ).isEqualTo(Link("http://localhost/v1/subjects/id", "self"))
    }
}
