package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.presentation.tag.TagResource
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetTagTest {
    @Test
    fun `tag when found returns resource`() {
        val tagRepository = mock<TagRepository>()
        whenever(tagRepository.findByIds(listOf("id"))).thenReturn(
            listOf(
                Tag(
                    TagId("id"),
                    "name"
                )
            )
        )

        assertThat(GetTag(tagRepository)("id")).isEqualTo(TagResource("id", "name"))
    }

    @Test
    fun `when tag not found throws`() {
        assertThrows<ResourceNotFoundApiException> {
            GetTag(mock())("not found")
        }
    }
}
