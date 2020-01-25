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
    fun `replace subject updates with new subjects`() {
        val disciplineResource = createDiscipline.invoke(CreateDisciplineRequest("colombia", "COL"))
        assertThat(disciplineResource.subjects).isEmpty()

        val subject = createSubject.invoke(CreateSubjectRequest("bogota"))
        val discipline = replaceDisciplineSubjects.invoke(disciplineResource.id.value, listOf(subject.id.value))

        assertThat(discipline.subjects.map { it.id.value }).containsExactly(subject.id.value)
        assertThat(discipline.name).isEqualTo("colombia")
    }
}
