package com.boclips.videos.service.infrastructure.subject.mongo.converters

import com.boclips.videos.service.domain.model.subjects.Subject
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.infrastructure.subject.SubjectDocumentConverter
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubjectDocumentConverterTest {

    @Test
    fun `convert a subject to document and back`() {
        val originalSubject = Subject(
            id = SubjectId(aValidId()), name = "maths"
        )

        val document = SubjectDocumentConverter.toSubjectDocument(originalSubject)
        val retrievedSubjet = SubjectDocumentConverter.toSubject(document)

        assertThat(originalSubject).isEqualTo(retrievedSubjet)
    }
}
