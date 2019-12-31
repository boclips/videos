package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.api.response.tag.TagResource
import com.boclips.videos.service.config.security.UserRoles.VIEW_TAGS
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

class TagsLinkBuilderTest {
    private lateinit var tagsLinkBuilder: TagsLinkBuilder

    @BeforeEach
    internal fun setUp() {
        setSecurityContext("bambi", VIEW_TAGS)
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        tagsLinkBuilder = TagsLinkBuilder(mock)
    }

    @Test
    fun tags() {
        assertThat(tagsLinkBuilder.tags()).isEqualTo(Link("https://localhost/v1/tags", "tags"))
    }

    @Test
    fun `tags link with rel`() {
        assertThat(tagsLinkBuilder.tags("rel")).isEqualTo(Link("https://localhost/v1/tags", "rel"))
    }

    @Test
    fun `tag link defaults to self`() {
        assertThat(tagsLinkBuilder.tag(TagResource("id"))).isEqualTo(
            Link(
                "https://localhost/v1/tags/id",
                "self"
            )
        )
    }

    @Test
    fun `tag link with rel`() {
        assertThat(
            tagsLinkBuilder.tag(
                TagResource("id"),
                "rel"
            )
        ).isEqualTo(Link("https://localhost/v1/tags/id", "rel"))
    }

    @Test
    fun `when no view tags role`() {
        setSecurityContext("bambi")
        assertThat(tagsLinkBuilder.tags()).isNull()
        assertThat(tagsLinkBuilder.tag(TagResource("id"))).isNull()
    }
}
