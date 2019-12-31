package com.boclips.videos.service.application.disciplines

import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.converters.DisciplineConverter
import com.boclips.videos.service.testsupport.DisciplineFactory
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ReplaceDisciplineSubjectsTest {

    @Test
    fun `replace subject updates with new subjects`(@Mock disciplineRepository: DisciplineRepository, @Mock subjectRepository: SubjectRepository) {
        val replaceDisciplineSubjects = ReplaceDisciplineSubjects(disciplineRepository, subjectRepository)

        val originalDiscipline = DisciplineFactory.sample()
        whenever(disciplineRepository.findOne("discipline-id")).thenReturn(originalDiscipline)

        val newDiscipline = replaceDisciplineSubjects(
            "discipline-id",
            listOf("https://example.com/subjects/subject-1", "https://example.com/subjects/subject-1")
        )

        assertThat(newDiscipline).isEqualTo(DisciplineConverter.from(originalDiscipline.copy(subjects = listOf())))
    }
}
