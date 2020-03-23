package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.request.discipline.CreateDisciplineRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReplaceDisciplineSubjectsTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var replaceDisciplineSubjects: ReplaceDisciplineSubjects

    @Autowired
    lateinit var createDiscipline: CreateDiscipline

    @Test
    fun `replace subject updates with new subjects and persists ordering`() {
        val disciplineResource = createDiscipline.invoke(CreateDisciplineRequest("colombia", "COL"))
        assertThat(disciplineResource.subjects).isEmpty()

        val subject = createSubject.invoke(CreateSubjectRequest("bogota"))
        val subject2 = createSubject.invoke(CreateSubjectRequest("subject2"))
        val subject3 = createSubject.invoke(CreateSubjectRequest("subject3"))
        val discipline = replaceDisciplineSubjects.invoke(disciplineResource.id.value, listOf(subject2.id.value, subject.id.value, subject3.id.value))

        assertThat(discipline.name).isEqualTo("colombia")
        assertThat(discipline.subjects[0].id.value).isEqualTo(subject2.id.value)
        assertThat(discipline.subjects[1].id.value).isEqualTo(subject.id.value)
        assertThat(discipline.subjects[2].id.value).isEqualTo(subject3.id.value)
    }
}
