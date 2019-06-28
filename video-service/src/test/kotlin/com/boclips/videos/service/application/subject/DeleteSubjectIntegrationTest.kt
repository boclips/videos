package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.subjects.SubjectRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteSubjectIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Autowired
    lateinit var deleteSubject: DeleteSubject

    @Test
    fun `subject when found returns resource`() {
        val subject = subjectRepository.create("Biology")

        deleteSubject(subject.id)

        assertThat(subjectRepository.findById(subject.id)).isNull()
    }
}
