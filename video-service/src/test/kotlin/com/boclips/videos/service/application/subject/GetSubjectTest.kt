package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.subject.SubjectResource
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetSubjectTest {
    @Test
    fun `subject when found returns resource`() {
        val subjectRepository = mock<SubjectRepository>()
        whenever(subjectRepository.findByIds(listOf("id"))).thenReturn(
            listOf(
                Subject(
                    SubjectId("id"),
                    "name"
                )
            )
        )

        assertThat(GetSubject(subjectRepository)("id")).isEqualTo(SubjectResource("id", "name"))
    }

    @Test
    fun `when subject not found throws`() {
        assertThrows<ResourceNotFoundApiException> {
            GetSubject(mock())("not found")
        }
    }
}
